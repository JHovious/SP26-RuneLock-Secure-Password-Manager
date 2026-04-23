import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.time.LocalDate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;


/**
 *
 * @author ckurd, Justin Hovious
 */
public class JSONControl {
    private User user;          // main user object
    private Boolean check;      // login success flag

    private ObjectMapper myMapper;
    private int newFileNum;

    private Security cipherTool = null;

    //Constructor
    public JSONControl() {
        this.user = null;
        this.check = false;

        this.myMapper = new ObjectMapper();
        myMapper.registerModule(new JavaTimeModule());
        myMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        newFileNum = 0;
    }

    public User getUser() {
        return this.user;
    }

    // Encrypted login
    public Boolean verifyLogin(String username, String password) {
        this.user = null;
        this.check = false;
        this.cipherTool = null;

        try {
            File folder = new File("MainAccounts");
            if (!folder.exists()) {
                return false;
            }

            File[] files = folder.listFiles();
            if (files == null) return false;

            for (File f : files) {
                if (!f.isFile() || !f.getName().endsWith(".enc")) continue;

                // baseName is the uid/mainFileNum, e.g. "1", "2", ...
                String baseName = f.getName().substring(0, f.getName().lastIndexOf('.'));

                try {
                    // Try a keystore bound to THIS file's id
                    Security sec = new Security(username, password, baseName);

                    byte[] encryptedUser = Files.readAllBytes(f.toPath());
                    byte[] plainUser = sec.decrypt(encryptedUser);
                    String jsonObject = convertToString(plainUser);
                    User thisUser = myMapper.readValue(jsonObject, User.class);

                    if (thisUser.username.equals(username) && thisUser.password.equals(password)) {
                        this.user = thisUser;
                        this.check = true;
                        this.cipherTool = sec;  // keep the matching Security instance
                        return true;
                    }
                } catch (Exception ex) {
                    // Wrong keystore / wrong password / not this user → try next file
                    continue;
                }
            }

            return false;

        } catch (Exception e) {
            System.out.println("Error during verifyLogin: " + e.getMessage());
            this.check = false;
            this.user = null;
            this.cipherTool = null;
            return false;
        }
    }




    // Load all encrypted account files for main user + subusers
    public void loadAccountFiles() {
      try {
          this.user.accounts.clear();

          if (this.cipherTool == null) {
              this.cipherTool = new Security(
                  this.user.username,
                  this.user.password,
                  this.user.mainFileNum
              );
          }

          // Main User Vault
          String folderPath = "Accounts/" + user.getuid();
          File folder = new File(folderPath);
          if (!folder.exists()) {
              folder.mkdirs();
          }

          File[] files = folder.listFiles();
          if (files != null) {
              for (File f : files) {
                  if (f.isFile() && f.getName().endsWith(".enc")) {
                      byte[] encryptedAccount = Files.readAllBytes(f.toPath());
                      byte[] plainAccount = cipherTool.decrypt(encryptedAccount);
                      String jsonObject = convertToString(plainAccount);
                      Account account = myMapper.readValue(jsonObject, Account.class);
                      account.setOwnerUid(this.user.getuid());
                      this.user.accounts.add(account);
                      System.out.println("Loaded account file: " + f.getName());
                  }
              }
          }

          // Sub User Vaults (Admin View)
          for (User.SubUserMeta su : user.subAccounts) {
              try {
                  // Decrypt subuser password using MAIN user's key
                  byte[] encPwdBytes = Base64.getDecoder().decode(su.encPassword);
                  String subPassword = convertToString(cipherTool.decrypt(encPwdBytes));

                  // Build Security instance for the subuser
                  Security subSec = new Security(su.username, subPassword, su.uid);

                  // Load and decrypt subuser vault files
                  String subFolder = "Accounts/" + su.uid;
                  File subDir = new File(subFolder);
                  if (!subDir.exists()) continue;

                  File[] subFiles = subDir.listFiles();
                  if (subFiles != null) {
                      for (File f : subFiles) {
                          if (f.isFile() && f.getName().endsWith(".enc")) {
                              byte[] encryptedAccount = Files.readAllBytes(f.toPath());
                              byte[] plainAccount = subSec.decrypt(encryptedAccount);
                              String jsonObject = convertToString(plainAccount);
                              Account acc = myMapper.readValue(jsonObject, Account.class);
                              acc.setOwnerUid(su.uid);
                              this.user.accounts.add(acc);
                          }
                      }
                  }

              } catch (Exception ex) {
                  System.out.println(
                      "Error loading subuser vault for uid " + su.uid + ": " + ex.getMessage()
                  );
              }
          }

      } catch (Exception e) {
          System.out.println("Error loading accounts " + e.getMessage());
          e.printStackTrace();
      }
  }


    // Add new account and save encrypted
    public void addAccount(String aUrl, String aUsername, String aPassword, ArrayList tags) {
        LocalDate createdDate = LocalDate.now();
        LocalDate lastUsed = createdDate;
        Account newAccount = new Account(aUrl, aUsername, aPassword, tags, createdDate, lastUsed, this.user.accountFileNum);
        this.user.incrementVaultVersion();
        storeAccountData(newAccount);
    }

    // Store encrypted account file
    public void storeAccountData(Account anAccount) {
        try {
            String folderPath = "Accounts/" + user.getuid() + "/";
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            if (this.cipherTool == null) {
                this.cipherTool = new Security(this.user.username, this.user.password, this.user.mainFileNum);
            }

            String jsonAccount = myMapper.writeValueAsString(anAccount);
            byte[] encrypted = cipherTool.encrypt(convertToBytes(jsonAccount));
            Path path = Paths.get(folderPath, this.user.accountFileNum + ".enc");
            Files.write(path, encrypted);

            int fileNumber = Integer.parseInt(this.user.accountFileNum);
            fileNumber += 1;
            this.user.accountFileNum = String.valueOf(fileNumber);

            storeMainAccount(this.user.username, this.user.password);
            System.out.println("Success adding account (encrypted)");

        } catch (Exception e) {
            System.out.println("There was an error saving account info");
            System.out.println(e);
        }
    }

    // Update encrypted account
    public void updateAccount(Account updated) {
        try {
            if (this.cipherTool == null) {
                this.cipherTool = new Security(this.user.username, this.user.password, this.user.mainFileNum);
            }

            String folderPath = "Accounts/" + user.getuid();
            String jsonAccount = myMapper.writeValueAsString(updated);
            byte[] encrypted = cipherTool.encrypt(convertToBytes(jsonAccount));
            Path path = Paths.get(folderPath, updated.getFileNumber() + ".enc");
            Files.write(path, encrypted);

            this.user.incrementVaultVersion();
            storeMainAccount(this.user.username, this.user.password);
            System.out.println("Account updated (encrypted): " + path.toString());

        } catch (Exception e) {
            System.out.println("Error updating account " + e.getMessage());
        }
    }

    // Delete encrypted account
    public void deleteAccount(Account target) {
        try {
            String filePath = "Accounts/" + user.getuid() + "/" + target.getFileNumber() + ".enc";
            File file = new File(filePath);

            if (file.exists()) {
                file.delete();
                System.out.println("Account deleted: " + filePath);
            }

            user.getAccounts().remove(target);
            this.user.incrementVaultVersion();
            storeMainAccount(user.getUsername(), user.getPassword());

        } catch (Exception e) {
            System.out.println("Error deleting account: " + e.getMessage());
        }
    }

    // Encrypted main account save
    public void storeMainAccount(String username, String password) {
        try {
            File folder = new File("MainAccounts");
            if (!folder.exists()) folder.mkdirs();

            if (this.cipherTool == null) {
                this.cipherTool = new Security(username, password, this.user.mainFileNum);
            }

            String jsonUser = myMapper.writeValueAsString(this.user);
            byte[] encrypted = cipherTool.encrypt(convertToBytes(jsonUser));
            Path path = Paths.get("MainAccounts", this.user.mainFileNum + ".enc");
            Files.write(path, encrypted);

        } catch (Exception e) {
            System.out.println("Error saving main account info (encrypted)");
            e.printStackTrace();
        }
    }

    // Create new main account (encrypted-only)
    public void createMainAccount(String aUsername, String aPassword) {
        try {
            File folder = new File("MainAccounts");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String newId = generateNewMainFileNum(); // used for both uid and mainFileNum
            User newUser = new User(aUsername, aPassword, newId, newId);

            this.cipherTool = new Security(aUsername, aPassword, newId);
            this.user = newUser;

            storeMainAccount(aUsername, aPassword);

        } catch (Exception e) {
            System.out.println("There was an error creating a new account");
            System.out.println(e);
        }
    }

    // Create subuser as full User, encrypted main account, and vault folder
    public void storeSubAccount(String name, String username, String password) {
        try {
            String newUid = generateNewUid();
            String newMainFileNum = newUid;

            // Create the subuser as a full user object
            User subUser = new User(username, password, newUid, newMainFileNum);
            subUser.isSub = true;
            subUser.parentUid = this.user.uid;

            // Create keystore and encrypted main account for subuser
            Security subSec = new Security(username, password, newMainFileNum);
            String jsonSubUser = myMapper.writeValueAsString(subUser);
            byte[] encryptedSubUser = subSec.encrypt(convertToBytes(jsonSubUser));
            Path subPath = Paths.get("MainAccounts", newMainFileNum + ".enc");
            Files.write(subPath, encryptedSubUser);

            // Create vault folder for subuser
            File vaultFolder = new File("Accounts/" + newUid);
            if (!vaultFolder.exists()) vaultFolder.mkdirs();

            // Encrypt subuser password with MAIN user's key
            if (this.cipherTool == null) {
                this.cipherTool = new Security(this.user.username, this.user.password, this.user.mainFileNum);
            }
            byte[] encPwdBytes = cipherTool.encrypt(convertToBytes(password));
            String encPwdBase64 = Base64.getEncoder().encodeToString(encPwdBytes);

            // Add metadata to parent user
            User.SubUserMeta meta = new User.SubUserMeta(newUid, username, name, encPwdBase64);
            this.user.subAccounts.add(meta);

            // Save parent user (encrypted)
            storeMainAccount(this.user.username, this.user.password);

            System.out.println("Created SubUser: UID = " + newUid);

        } catch (Exception e) {
            System.out.println("Error creating subuser");
            e.printStackTrace();
        }
    }


    // Remove subuser (main account and vault)
    public void deleteSubUser(User.SubUserMeta target) {
        try {
            // Delete subuser main account file (we use uid == mainFileNum)
            File subMain = new File("MainAccounts/" + target.uid + ".enc");
            if (subMain.exists()) {
                subMain.delete();
                System.out.println("SubUser main account deleted: " + subMain.getAbsolutePath());
            }

            // Delete subuser vault folder
            File vault = new File("Accounts/" + target.uid);
            if (vault.exists()) {
                File[] vaultFiles = vault.listFiles();
                if (vaultFiles != null) {
                    for (File vf : vaultFiles) {
                        vf.delete();
                    }
                }
                vault.delete();
                System.out.println("SubUser vault deleted: " + vault.getAbsolutePath());
            }

            // Remove metadata from parent
            this.user.subAccounts.removeIf(su -> su.uid.equals(target.uid));
            storeMainAccount(this.user.username, this.user.password);

        } catch (Exception e) {
            System.out.println("Couldn't delete subuser" + e.getMessage());
        }
    }

    // Update subuser credentials (main account only)
    public void updateSubUser(User.SubUserMeta updated, String newUsername, String newPassword) {
        try {
            // Load, decrypt, update, and re-encrypt the subuser main account
            String subId = updated.uid;
            File subFile = new File("MainAccounts/" + subId + ".enc");
            if (subFile.exists()) {

                Security subCipher = new Security(newUsername, newPassword, subId);

                // Rebuild subUser object
                User subUser = new User(newUsername, newPassword, subId, subId);

                String jsonSubUser = myMapper.writeValueAsString(subUser);
                byte[] encrypted = subCipher.encrypt(convertToBytes(jsonSubUser));
                Path path = Paths.get("MainAccounts", subId + ".enc");
                Files.write(path, encrypted);

                System.out.println("SubUser main account updated: " + path.toString());
            }

            // Update metadata
            updated.username = newUsername;
            storeMainAccount(this.user.username, this.user.password);

        } catch (Exception e) {
            System.out.println("Error updating subuser: " + e.getMessage());
        }
    }


    // Generate new numeric ID based on MainAccounts/*.enc filenames
    private String generateNewMainFileNum() {
        try {
            File folder = new File("MainAccounts");
            if (!folder.exists()) folder.mkdirs();

            int maxFileNum = 0;
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile() && f.getName().endsWith(".enc")) {
                        String name = f.getName().substring(0, f.getName().length() - 4); // strip .enc
                        try {
                            int num = Integer.parseInt(name);
                            if (num > maxFileNum) maxFileNum = num;
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            return String.valueOf(maxFileNum + 1);

        } catch (Exception e) {
            System.out.println("Error generating mainFileNum");
            return "1";
        }
    }

    // Tag recommender

    public String recommendTag(String url) {
        url = url.toLowerCase();

        if (url.contains("facebook") || url.contains("reddit") || url.contains("instagram") || url.contains("tiktok"))
            return "Social Media";

        if (url.contains("amazon") || url.contains("ebay") || url.contains("walmart") || url.contains("target"))
            return "Shopping";

        if (url.contains("gmail") || url.contains("outlook"))
            return "Email";

        if (url.contains("bank") || url.contains("credit"))
            return "Finance";

        if (url.contains("edu") || url.contains("k12") || url.contains(".org") || url.contains(".net"))
            return "Education";

        if (url.contains("steam") || url.contains("game") || url.contains("gog") || url.contains("twitch"))
            return "Gaming";

        return "General";
    }

    // Helper methods

    private byte[] convertToBytes(String stringObject) {
        return stringObject.getBytes(StandardCharsets.UTF_8);
    }

    private String convertToString(byte[] plainText) {
        return new String(plainText, StandardCharsets.UTF_8);
    }
    
    public boolean isAuthorizedDevice(String deviceId) {
        User user = getUser();
        if (user == null) return false;
        return user.isDeviceAuthorized(deviceId);
    }

    
    public void handleUpdates(int whichUpdated) {
        try {
            // Ensure cipherTool exists
            if (this.cipherTool == null) {
                this.cipherTool = new Security(
                    this.user.username,
                    this.user.password,
                    this.user.mainFileNum
                );
            }

            // Update keystore (0 = username changed, 1 = password changed)
            cipherTool.updateKeystore(this.user, whichUpdated);

            // Save updated encrypted main account
            storeMainAccount(this.user.username, this.user.password);

            System.out.println("Keystore updated and main account re-encrypted.");

        } catch (Exception e) {
            System.out.println("Error handling keystore updates: " + e.getMessage());
        }
    }
    
        private String generateNewUid() {
        try {
            File folder = new File("MainAccounts");
            if (!folder.exists()) folder.mkdirs();

            int maxUid = 0;
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile() && f.getName().endsWith(".enc")) {
                        String name = f.getName().substring(0, f.getName().lastIndexOf('.'));
                        try {
                            int val = Integer.parseInt(name);
                            if (val > maxUid) maxUid = val;
                        } catch (NumberFormatException ignore) {}
                    }
                }
            }
            return String.valueOf(maxUid + 1);

        } catch (Exception e) {
            System.out.println("Error generating UID");
            return "1";
        }
    }


}
