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
    
    private final JSONControl controller;
    private final int syncPort;
    private volatile boolean running = false;
    
    private Thread broadcastThread;
    private Thread listenThread;
    private Thread syncServerThread;
    
    private DatagramSocket broadcastSocket;
    private DatagramSocket listenSocket;
    private ServerSocket syncServerSocket;
    
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
    }
    
    public void start() {
        if (running) return;
        running = true;
        
        startBroadcastThread();
        startListenThread();
        startSyncServerThread();
    }
    
    public void stop() {
        running = false;
        
        if (broadcastThread != null) broadcastThread.interrupt();
        if (listenThread != null) listenThread.interrupt();
        if (syncServerThread != null) syncServerThread.interrupt();
        
        try { if (broadcastSocket != null) broadcastSocket.close(); } catch (Exception ignored) {}
        try { if (listenSocket != null) listenSocket.close(); } catch (Exception ignored) {}
        try { if (syncServerSocket != null) syncServerSocket.close(); } catch (Exception ignored) {}
            
       System.out.println("LanSyncService stopped.");
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

            // Zip vault folder to memory.
            byte[] zipBytes = zipVaultFolder(user.getuid());

            // Send vaultVersion, zip length, and zip bytes.
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
                user.getuid() + "|" +
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
        
        // Ignore our own broadcasts
        if (deviceId.equals(user.getuid())) return;
        
        // Enforce device authorization at discovery time
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
        
        // Decide if we should sync from this peer.
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

            
            String myDeviceId = user.getuid();
            dataOut.writeUTF(myDeviceId);
            dataOut.flush();

            // Read vaultVersion and zip length
            int remoteVersion = dataIn.readInt();
            int zipLength = dataIn.readInt();

            if (zipLength < 0 || zipLength > 50_000_000) { // 50MB
                System.out.println("Invalid zip length from peer");
                return;
            }

            byte[] zipBytes = new byte[zipLength];
            dataIn.readFully(zipBytes);

            // Overwrite local vault folder with received zip.
            overwriteVaultFromZip(user.getuid(), zipBytes);

            // Update vaultVersion and save main account.
            user.setVaultVersion(remoteVersion);
            controller.storeMainAccount(user.getUsername(), user.getPassword());

            // Reload accounts into memory.
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
        
        // Delete existing files
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
        
        // Unzip into folder
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
    
    // Simple wrapper so LanSyncService doesn't know JSONControl internals.
    private boolean isAuthorizedDevice(String deviceId) {
        return controller.isAuthorizedDevice(deviceId);
    }
    

    
}
