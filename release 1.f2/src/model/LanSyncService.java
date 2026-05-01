package model;

import controller.JSONControl;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Justin Hovious
 */
public class LanSyncService {
    
    private static final int DISCOVERY_PORT = 50505;
    private static final int DISCOVERY_INTERVAL_MS = 3000;
    private static final String MAGIC = "RUNELOCK_DISCOVERY";
    
    // Bootstrap constants
    private static final int BOOTSTRAP_PORT = 50507;
    private static final String BOOTSTRAP_REQUEST_MAGIC = "RUNELOCK_BOOTSTRAP_REQUEST";
    private static final String BOOTSTRAP_RESPONSE_MAGIC = "RUNELOCK_BOOTSTRAP_RESPONSE";
    
    private final JSONControl controller;
    private final int syncPort;
    private final String localDeviceId;
    private volatile boolean running = false;
    
    private Thread broadcastThread;
    private Thread listenThread;
    private Thread syncServerThread;
    private Thread bootstrapListenThread;
    
    private DatagramSocket broadcastSocket;
    private DatagramSocket listenSocket;
    private ServerSocket syncServerSocket;
    private DatagramSocket bootstrapListenSocket;
    
    // Peer info holder
    private static class PeerInfo {
        String deviceId;
        String username;
        int vaultVersion;
        InetAddress address;
        int syncPort;
        long lastSeen;
    }
    
    private final Map<String, PeerInfo> peers = new ConcurrentHashMap<>();
    
    public LanSyncService(JSONControl controller, int syncPort) {
        this.controller = controller;
        this.syncPort = syncPort;
        this.localDeviceId = DeviceIdUtil.getDeviceId();
    }
    
    public void start() {
        if (running) return;
        running = true;
        
        startBroadcastThread();
        startListenThread();
        startSyncServerThread();
        startBootstrapListenThread();
    }
    
    public void stop() {
        running = false;
        
        if (broadcastThread != null) broadcastThread.interrupt();
        if (listenThread != null) listenThread.interrupt();
        if (syncServerThread != null) syncServerThread.interrupt();
        if (bootstrapListenThread != null) bootstrapListenThread.interrupt();
        
        try { if (broadcastSocket != null) broadcastSocket.close(); } catch (Exception ignored) {}
        try { if (listenSocket != null) listenSocket.close(); } catch (Exception ignored) {}
        try { if (syncServerSocket != null) syncServerSocket.close(); } catch (Exception ignored) {}
        try { if (bootstrapListenSocket != null) bootstrapListenSocket.close(); } catch (Exception ignored) {}
            
        System.out.println("LanSyncService stopped.");
    }
    
    /**
     * Broadcasts a bootstrap request for the given username and waits for a
     * peer to respond with the .enc and .p12 files needed to log in.
     * Called from App.java when MainAccounts/ is empty on a fresh device.
     *
     * @param username  The username typed into the login screen
     * @return true if files were received and saved successfully, false otherwise
     */
    public static boolean requestBootstrap(String username) {
        System.out.println("Broadcasting bootstrap request for user: " + username);
        
        try {
            // Broadcast UDP request so peers know we need files
            String requestPayload = BOOTSTRAP_REQUEST_MAGIC + "|" + username + "|" + DeviceIdUtil.getDeviceId();
            byte[] data = requestPayload.getBytes("UTF-8");
            
            DatagramSocket sendSocket = new DatagramSocket();
            sendSocket.setBroadcast(true);
            
            DatagramPacket packet = new DatagramPacket(
                data,
                data.length,
                InetAddress.getByName("255.255.255.255"),
                BOOTSTRAP_PORT
            );
            
            // Open a TCP server socket to receive the files
            ServerSocket receiveServer = new ServerSocket(0);
            int receivePort = receiveServer.getLocalPort();
            receiveServer.setSoTimeout(10000); // 10 second timeout waiting for a peer
            
            // Re-build payload with our receive port included
            String fullPayload = BOOTSTRAP_REQUEST_MAGIC + "|" + username + "|" + DeviceIdUtil.getDeviceId() + "|" + receivePort;
            byte[] fullData = fullPayload.getBytes("UTF-8");
            DatagramPacket fullPacket = new DatagramPacket(
                fullData,
                fullData.length,
                InetAddress.getByName("255.255.255.255"),
                BOOTSTRAP_PORT
            );
            
            // Broadcast a few times to improve reliability
            for (int i = 0; i < 3; i++) {
                sendSocket.send(fullPacket);
                Thread.sleep(300);
            }
            sendSocket.close();
            
            System.out.println("Bootstrap request sent. Waiting for peer response on port " + receivePort + "...");
            
            // Wait for a peer to connect and send the files
            try {
                Socket peer = receiveServer.accept();
                boolean success = receiveBootstrapFiles(peer);
                receiveServer.close();
                return success;
            } catch (SocketTimeoutException e) {
                System.out.println("Bootstrap timed out. No peer responded.");
                receiveServer.close();
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("Bootstrap request error: " + e.getMessage());
            return false;
        }
    }
    

    // Receives and saves the .enc and .p12 files sent by a responding peer.
    private static boolean receiveBootstrapFiles(Socket peer) {
        try (Socket socket = peer;
             DataInputStream dataIn = new DataInputStream(socket.getInputStream())) {
            
            String magic = dataIn.readUTF();
            if (!magic.equals(BOOTSTRAP_RESPONSE_MAGIC)) {
                System.out.println("Bootstrap: unexpected response magic: " + magic);
                return false;
            }
            
            // Read .enc file
            String encFileName = dataIn.readUTF();
            int encLength = dataIn.readInt();
            byte[] encBytes = new byte[encLength];
            dataIn.readFully(encBytes);
            
            // Read .p12 file
            String p12FileName = dataIn.readUTF();
            int p12Length = dataIn.readInt();
            byte[] p12Bytes = new byte[p12Length];
            dataIn.readFully(p12Bytes);
            
            // Save .enc to MainAccounts/
            File mainAccountsDir = new File("MainAccounts");
            if (!mainAccountsDir.exists()) mainAccountsDir.mkdirs();
            Files.write(Paths.get("MainAccounts", encFileName), encBytes);
            
            // Save .p12 to Security/
            File securityDir = new File("Security");
            if (!securityDir.exists()) securityDir.mkdirs();
            Files.write(Paths.get("Security", p12FileName), p12Bytes);
            
            System.out.println("Bootstrap complete. Received: " + encFileName + ", " + p12FileName);
            return true;
            
        } catch (Exception e) {
            System.out.println("Error receiving bootstrap files: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Listens for bootstrap requests from new devices and responds with the
     * requested user's .enc and .p12 files if they exist locally.
     */
    private void startBootstrapListenThread() {
        bootstrapListenThread = new Thread(() -> {
            try {
                bootstrapListenSocket = new DatagramSocket(BOOTSTRAP_PORT);
                byte[] buf = new byte[1024];
                System.out.println("Bootstrap listener started on port " + BOOTSTRAP_PORT);
                
                while (running) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        bootstrapListenSocket.receive(packet);
                        
                        String msg = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                        InetAddress requesterAddress = packet.getAddress();
                        
                        handleBootstrapRequest(msg, requesterAddress);
                        
                    } catch (IOException ex) {
                        if (running) {
                            System.out.println("Bootstrap listen error: " + ex.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not start bootstrap listener: " + e.getMessage());
            }
        }, "RuneLock-LAN-BootstrapListen");
        
        bootstrapListenThread.setDaemon(true);
        bootstrapListenThread.start();
    }
    
    /**
     * Handles an incoming bootstrap request. Checks if the requested username
     * exists locally, then connects back to the requester and sends the files.
     */
    private void handleBootstrapRequest(String msg, InetAddress requesterAddress) {
        try {
            if (!msg.startsWith(BOOTSTRAP_REQUEST_MAGIC)) return;
            
            String[] parts = msg.split("\\|");
            if (parts.length < 4) return;
            
            String requestedUsername = parts[1];
            String requesterDeviceId = parts[2];
            int requesterReceivePort = Integer.parseInt(parts[3]);
            
            // Don't respond to our own requests
            if (requesterDeviceId.equals(this.localDeviceId)) return;
            
            System.out.println("Bootstrap request received for username: " + requestedUsername +
                               " from device: " + requesterDeviceId);
            
            // Find the .enc file that matches this username
            User currentUser = controller.getUser();
            if (currentUser == null) return;
            
            // Check if the request is for the currently logged-in user
            // or one of their subusers
            String targetFileNum = null;
            
            if (currentUser.getUsername().equals(requestedUsername)) {
                targetFileNum = currentUser.getMainFileNum();
            } else {
                // Check subusers
                for (User.SubUserMeta su : currentUser.getSubAccounts()) {
                    if (su.username.equals(requestedUsername)) {
                        targetFileNum = su.uid;
                        break;
                    }
                }
            }
            
            if (targetFileNum == null) {
                System.out.println("Bootstrap: no matching user found for: " + requestedUsername);
                return;
            }
            
            File encFile = new File("MainAccounts/" + targetFileNum + ".enc");
            File p12File = new File("Security/" + targetFileNum + ".p12");
            
            if (!encFile.exists() || !p12File.exists()) {
                System.out.println("Bootstrap: required files not found for: " + requestedUsername);
                return;
            }
            
            // Connect back to the requester and send the files
            sendBootstrapFiles(requesterAddress, requesterReceivePort, encFile, p12File);
            
        } catch (Exception e) {
            System.out.println("Error handling bootstrap request: " + e.getMessage());
        }
    }
    
    // Connects to the requesting device and sends the .enc and .p12 files.
    private void sendBootstrapFiles(InetAddress address, int port, File encFile, File p12File) {
        Thread sendThread = new Thread(() -> {
            try (Socket socket = new Socket(address, port);
                 DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream())) {
                
                byte[] encBytes = Files.readAllBytes(encFile.toPath());
                byte[] p12Bytes = Files.readAllBytes(p12File.toPath());
                
                dataOut.writeUTF(BOOTSTRAP_RESPONSE_MAGIC);
                
                // Send .enc file
                dataOut.writeUTF(encFile.getName());
                dataOut.writeInt(encBytes.length);
                dataOut.write(encBytes);
                
                // Send .p12 file
                dataOut.writeUTF(p12File.getName());
                dataOut.writeInt(p12Bytes.length);
                dataOut.write(p12Bytes);
                
                dataOut.flush();
                System.out.println("Bootstrap files sent to " + address.getHostAddress() + ":" + port);
                
            } catch (Exception e) {
                System.out.println("Error sending bootstrap files: " + e.getMessage());
            }
        }, "RuneLock-LAN-BootstrapSend");
        
        sendThread.setDaemon(true);
        sendThread.start();
    }
    
    private void startSyncServerThread() {
        syncServerThread = new Thread(() -> {
            try {
                syncServerSocket = new ServerSocket(syncPort);
                System.out.println("RuneLock sync server listening on port" + syncPort);
                
                while (running) {
                    try { 
                        Socket client = syncServerSocket.accept();
                        handleSyncClient(client);
                    } catch (IOException e) {
                        if (running) {
                            System.out.println("Sync server error: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Could not start sync server: " + e.getMessage());
            }
        }, "RuneLock-LAN-SyncServer");
        
        syncServerThread.setDaemon(true);
        syncServerThread.start();
    }
    
    private void handleSyncClient(Socket client) {
        try (Socket socket = client;
             InputStream in = socket.getInputStream();
             DataInputStream dataIn = new DataInputStream(in);
             OutputStream out = socket.getOutputStream();
             DataOutputStream dataOut = new DataOutputStream(out)) {

            User user = controller.getUser();
            if (user == null) return;

            String remoteDeviceId = dataIn.readUTF();

            if (!isAuthorizedDevice(remoteDeviceId)) {
                System.out.println("Rejected unauthorized sync request from device: " + remoteDeviceId);
                return;
            }

            byte[] zipBytes = zipVaultFolder(user.getuid());

            dataOut.writeInt(user.getVaultVersion());
            dataOut.writeInt(zipBytes.length);
            dataOut.write(zipBytes);
            dataOut.flush();

            System.out.println("Sent vault to authorized peer " + remoteDeviceId +
                               ", version " + user.getVaultVersion());
        } catch (Exception e) {
            System.out.println("Error handling sync client: " + e.getMessage());
        }
    }

    
    private byte[] zipVaultFolder(String uid) throws IOException {
        String folderPath = "Accounts/" + uid;
        Path folder = Paths.get(folderPath);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            if (!Files.exists(folder)) {
                return new byte[0];
            }
            
            Files.list(folder).forEach(path -> {
                if (Files.isRegularFile(path)) {
                    try (InputStream in = Files.newInputStream(path)) {
                        ZipEntry entry = new ZipEntry(path.getFileName().toString());
                        zos.putNextEntry(entry);
                        
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                        zos.closeEntry();
                    } catch (IOException e) {
                        System.out.println("Error zipping file: " + e.getMessage());
                    }
                }
            });
        }
        return baos.toByteArray();
    }
    
    private void startBroadcastThread() {
        broadcastThread = new Thread(() -> {
            try {
                broadcastSocket = new DatagramSocket();
                broadcastSocket.setBroadcast(true);

                
                while (running) {
                    try {
                        String payload = buildDiscoveryPayload();
                        byte[] data = payload.getBytes("UTF-8");
                        
                        DatagramPacket packet = new DatagramPacket(
                                data,
                                data.length,
                                InetAddress.getByName("255.255.255.255"),
                                DISCOVERY_PORT
                        );
                        
                        broadcastSocket.send(packet);
                        
                        Thread.sleep(DISCOVERY_INTERVAL_MS);
                    } catch (InterruptedException ex) {
                        break;
                    } catch (Exception ex) {
                        System.out.println("Broadcast error: " + ex.getMessage());
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not open broadcast socket: " + e.getMessage());
            }
        }, "RuneLock-LAN-Broadcast");
        
        broadcastThread.setDaemon(true);
        broadcastThread.start();
    }
    
    private void startListenThread() {
        listenThread = new Thread(() -> {
           try {
               listenSocket = new DatagramSocket(DISCOVERY_PORT);
               byte[] buf = new byte[1024];
               
               while (running) {
                   try {
                       DatagramPacket packet = new DatagramPacket(buf, buf.length);
                       listenSocket.receive(packet);
                       
                       String msg = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                       handleDiscoveryPacket(msg, packet.getAddress());
                   } catch (IOException ex) {
                       if (running) {
                           System.out.println("Listen error: " + ex.getMessage());
                       }
                   }
               }
           } catch (Exception e) {
               System.out.println("Could not open listen socket: " + e.getMessage());
           }
        }, "RuneLock-LAN-Listen");
        
        listenThread.setDaemon(true);
        listenThread.start();
    }
    
    private String buildDiscoveryPayload() {
        User user = controller.getUser();
        if (user == null) return "";
        
        return MAGIC + "|" +
                localDeviceId + "|" +
                user.getUsername() + "|" +
                user.getVaultVersion() + "|" +
                syncPort;
    }
    
    private void handleDiscoveryPacket(String msg, InetAddress from) {
        try {
            if (!msg.startsWith(MAGIC)) return;
        
        String[] parts = msg.split("\\|");
        if (parts.length < 5) return;
        
        String deviceId = parts[1];
        String username = parts[2];
        int vaultVersion = Integer.parseInt(parts[3]);
        int peerSyncPort = Integer.parseInt(parts[4]);
        
        User user = controller.getUser();
        if (user == null) return;
        
        if (deviceId.equals(localDeviceId)) return;
        
        if (!isAuthorizedDevice(deviceId)) {
            System.out.println("Ignoring unauthorized peer device: " + deviceId + " (" + username + ")");
            return;
        }
        
        PeerInfo info = new PeerInfo();
        info.deviceId = deviceId;
        info.username = username;
        info.vaultVersion = vaultVersion;
        info.address = from;
        info.syncPort = peerSyncPort;
        info.lastSeen = System.currentTimeMillis();
        
        peers.put(deviceId, info);
        
        maybeSyncFromPeer(info);
        
        } catch (Exception e) {
            System.out.println("Discover packet error: " + e.getMessage());
        }

    }
    
    private void maybeSyncFromPeer(PeerInfo peer) {
        User user = controller.getUser();
        if (user == null) return;
        
        int myVersion = user.getVaultVersion();
        
        if (peer.vaultVersion > myVersion) {
            System.out.println("Found newer vault on " + peer.username + 
                               " (" + peer.deviceId + "), version " + peer.vaultVersion +
                               " > " + myVersion + " - syncing now.");
            syncFromPeer(peer);
        }
    }
    
    private void syncFromPeer(PeerInfo peer) {
        try (Socket socket = new Socket(peer.address, peer.syncPort);
             InputStream in = socket.getInputStream();
             DataInputStream dataIn = new DataInputStream(in);
             OutputStream out = socket.getOutputStream();
             DataOutputStream dataOut = new DataOutputStream(out)) {

            User user = controller.getUser();
            if (user == null) return;

            dataOut.writeUTF(localDeviceId);
            dataOut.flush();

            int remoteVersion = dataIn.readInt();
            int zipLength = dataIn.readInt();

            if (zipLength < 0 || zipLength > 50_000_000) {
                System.out.println("Invalid zip length from peer");
                return;
            }

            byte[] zipBytes = new byte[zipLength];
            dataIn.readFully(zipBytes);

            overwriteVaultFromZip(user.getuid(), zipBytes);

            user.setVaultVersion(remoteVersion);
            controller.storeMainAccount(user.getUsername(), user.getPassword());

            controller.reloadUser();
            controller.loadAccountFiles();

            System.out.println("Vault synced from peer " + peer.username +
                               " to version " + remoteVersion);

        } catch (Exception e) {
            System.out.println("Error syncing from peer: " + e.getMessage());
        }
    }

    
    private void overwriteVaultFromZip(String uid, byte[] zipBytes) throws IOException {
        String folderPath = "Accounts/" + uid;
        Path folder = Paths.get(folderPath);
        
        if (Files.exists(folder)) {
            Files.list(folder).forEach(path -> {
               try {
                   Files.deleteIfExists(path);
               }  catch (IOException e) {
                   System.out.println("Error deleting old vault file: " + e.getMessage());
               }
            });
        } else {
            Files.createDirectories(folder);
        }
        
        try (ByteArrayInputStream bais = new ByteArrayInputStream(zipBytes);
                ZipInputStream zis = new ZipInputStream(bais)) {
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path outPath = folder.resolve(entry.getName());
                try (OutputStream out = Files.newOutputStream(outPath)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
                zis.closeEntry();
            }
        }
    }
    
    private boolean isAuthorizedDevice(String deviceId) {
        return controller.isAuthorizedDevice(deviceId);
    }
}
