import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.util.ArrayList;
import java.io.InputStream;
import java.time.LocalDate;
import java.io.FileOutputStream;
import java.io.OutputStream;
 


/**
 *
 * @author ckurd, jahov
 */
public class JSONControl {
    private User user;//Used for storing mainFile object info
    private Boolean check;//Used if file loaded successfully
    
    private ObjectMapper myMapper;
    private int newFileNum;
    
    
    //Constructor
    public JSONControl(){
        this.user = null;
        this.check = false;
        
        
        this. myMapper = new ObjectMapper();
        myMapper.registerModule(new JavaTimeModule());
        myMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        newFileNum = 0;

    }
    
    public User getUser() {
        return this.user;
    }
    
    //Method to verify initial login
    public Boolean verifyLogin(String username, String password){
        loadMainFile(username);
        
        if (!this.check || this.user == null) {
            return false;
        }
        
        return this.user.password.equals(password);
    }
    
    //Method to load the main id check file
    public void loadMainFile(String aUsername){
        this.check = false;
        this.user = null;
        
        // Creates MainAccounts folder if it doesn't exist
        try {
            File folder = new File("MainAccounts");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            
            // Searches the files until the correct info is found
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        User temp = myMapper.readValue(f, User.class);
                        if (aUsername.equals(temp.username)) {
                            this.user = temp;
                            this.check = true;
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading main file");
            this.check = false;
            this.user = null;
        }
    }
    
    //Method to load user account JSON files
    public void loadAccountFiles(){
        try{
            // Reset the list before loading to prevent file not found error
            this.user.accounts.clear();
            
            String folderPath = "Accounts/" + user.getuid();
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        Account account = myMapper.readValue(f, Account.class);
                        account.setOwnerUid(this.user.getuid()); // Mark as main user account
                        this.user.accounts.add(account);
                        System.out.println("This is the account fileName: " + account.getFileNumber() + ".json");
                    }
                }
            }
            for (User.SubUserMeta su : user.subAccounts) {
                String subFolder = "Accounts/" + su.uid;
                File subDir = new File(subFolder);
                
                if (!subDir.exists()) continue;
                
                File[] subFiles = subDir.listFiles();
                if (subFiles != null) {
                    for (File f : subFiles) {
                        if (f.isFile()) {
                            Account acc = myMapper.readValue(f, Account.class);
                            acc.setOwnerUid(su.uid); // mark as subuser account
                            this.user.accounts.add(acc);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading accounts" + e.getMessage());
            e.printStackTrace();
        }
    }

    
    //Method to add new account object and make file
    public void addAccount(String aUrl, String aUsername, String aPassword, ArrayList tags){
        LocalDate createdDate = LocalDate.now();
        LocalDate lastUsed = createdDate;
        Account newAccount = new Account(aUrl, aUsername, aPassword, tags, createdDate, lastUsed, this.user.accountFileNum);
        storeAccountData(newAccount);
    }
    
    //Method to store account files
    public void storeAccountData(Account anAccount){
        try{
            
            String folderPath = "Accounts/" + user.getuid() + "/";
            File folder = new File(folderPath);
            if (!folder.exists()){
                folder.mkdirs();
            }
            File currentFile = new File(folder, this.user.accountFileNum + ".json");
            System.out.println("Writing to: " + currentFile.getAbsolutePath());//testing for where currentFile is being saved
            System.out.println("This is current account file num: " + this.user.accountFileNum);
            
            ObjectWriter writer = myMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(currentFile, anAccount);
            int fileNumber = Integer.parseInt(this.user.accountFileNum);//Turn into integer
            fileNumber += 1;
            this.user.accountFileNum = String.valueOf(fileNumber);//Turn back into string
            storeMainAccount(this.user.username, this.user.password);//Method call to update user mainfile
            System.out.println("Success adding account");
            
        }catch (Exception e){
            System.out.println("There was an error saving account info");
            System.out.println(e);
        }
    }
    
    //Method to store sub account files
    public void storeSubAccount(String name, String username, String password){
        try{
            // Create new UID and mainFileNum for subuser
            String newUid = generateNewUid();
            String newMainFileNum = generateNewMainFileNum();
            
            // Create the subuser as a full user object
            User subUser = new User(username, password, newUid, newMainFileNum, this.user.uid);
            
            // Save subuser JSON in MainAccounts folder
            File folder = new File("MainAccounts");
            if (!folder.exists()) folder.mkdirs();
            
            File subUserFile = new File(folder, newMainFileNum + ".json");
            ObjectWriter writer = myMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(subUserFile, subUser);
            
            // Create vault folder for subuser
            File vaultFolder = new File("Accounts/" + newUid);
            if (!vaultFolder.exists()) vaultFolder.mkdirs();
            
            // Add metadata to parent user
            User.SubUserMeta meta = new User.SubUserMeta(newUid, username, name);
            this.user.subAccounts.add(meta);
            
            // Save parent user to JSON
            storeMainAccount(this.user.username, this.user.password);
            
            System.out.println("Created SubUser: UID = " + newUid);

        } catch(Exception e){
            System.out.println("Error creating subuser");
            e.printStackTrace();
        }
    }
    
    //Method to store mainAccount files
    public void storeMainAccount(String username, String password){
        try {
            File folder = new File("MainAccounts");
            if (!folder.exists()) folder.mkdirs();
            
            // Write to the file indicated by mainFileNum
            File targetFile = new File(folder, this.user.mainFileNum + ".json");
            ObjectWriter writer = myMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(targetFile, this.user);
            
        } catch (Exception e) {
            System.out.println("Error saving main account info");
            e.printStackTrace();
        }
    }
    
    //Method to create main accounts
    public void createMainAccount(String aUsername, String aPassword){
        //Pull filenumber from highest existing in files
        try{
            File folder = new File("MainAccounts");
            if (!folder.exists()){//If there are no main accounts make the folder for use later
                    folder.mkdirs();
            }
            ObjectWriter writer = myMapper.writerWithDefaultPrettyPrinter();
            File[] files = folder.listFiles();
            if (files != null){//If files exist
                int currentHigh;
                int newHigh = 0;
                int currentID;
                int newID = 0;
                User tempUser;

                for (File f : files){
                    if (f.isFile()){
                            tempUser = myMapper.readValue(f, User.class);
                            currentHigh = Integer.parseInt(tempUser.mainFileNum);
                            currentID = Integer.parseInt(tempUser.uid);
                            if (currentHigh > newHigh){//Find new filename for a user
                                newHigh = currentHigh;
                            }
                            if (currentID > newID){//Find new ID for a user
                                newID = currentID;
                            }
                    }
                }
                String newFileNum = incrementCounter(String.valueOf(newHigh));
                String newUID = incrementCounter(String.valueOf(newID));
                User newUser = new User(aUsername, aPassword, newUID, newFileNum);
                File newFile = new File(folder, newFileNum + ".json");
                writer.writeValue(newFile, newUser);
            }
                
        }catch (Exception e){
            System.out.println("There was an error creating a new account");
            System.out.println(e);
        }
    }
    
    public String incrementCounter(String aNumber){
        int number = Integer.parseInt(aNumber);//Turn into integer
        number += 1;
        return String.valueOf(number);//Turn back into string
    }
    
    // Method to update user accounts containing login credentials
    public void updateAccount(Account updated) {
        try {
            String filePath = "Accounts/" + user.getuid() + "/" + updated.getFileNumber() + ".json";
            ObjectWriter writer = myMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(new File(filePath), updated);
            System.out.println("Account updated: " + filePath);
            
        } catch (Exception e) {
            System.out.println("Error updating account" + e.getMessage());
        }
    }
    
    // Method to delete JSON file containing select login credentials
    public void deleteAccount(Account target) {
        try {
            String filePath = "Accounts/" + user.getuid() + "/" + target.getFileNumber() + ".json";
            File file = new File(filePath);
            
            if (file.exists()) {
                file.delete();
                System.out.println("Account deleted: " + filePath);
            }
            
            user.getAccounts().remove(target);
            storeMainAccount(user.getUsername(), user.getPassword());
            
        } catch (Exception e) {
            System.out.println("Error deleting account: " + e.getMessage());
        }
    }
    
    // Method to remove subUser from main account
    public void deleteSubUser(User.SubUserMeta target) {
        // Delete JSON file from subAccounts
    try {
        File folder = new File("MainAccounts");
        File[] files = folder.listFiles();
        
        if (files != null) {
            for(File f : files) {
                if (f.isFile()) {
                    User temp = myMapper.readValue(f, User.class);
                    if (temp.uid.equals(target.uid)) {
                        f.delete();
                        System.out.println("SubUser main account deleted: " + f.getAbsolutePath());
                    }
                }
            }
        }
        // Delete subuser vault folder
        File vault = new File ("Accounts/" + target.uid);
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
    
    // Method to update subuser information
    public void updateSubUser(User.SubUserMeta updated, String newUsername, String newPassword) {
        try {
            File folder = new File("MainAccounts");
            File[] files = folder.listFiles();

            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        User temp = myMapper.readValue(f, User.class);
                        if (temp.uid.equals(updated.uid)) {
                            temp.username = newUsername;
                            temp.password = newPassword;

                            ObjectWriter writer = myMapper.writerWithDefaultPrettyPrinter();
                            writer.writeValue(f, temp);
                            System.out.println("SubUser main account updated: " + f.getAbsolutePath());
                            break;
                        }
                    }
                }
            }

            // Update metadata
            updated.username = newUsername;
            storeMainAccount(this.user.username, this.user.password);

        } catch (Exception e) {
            System.out.println("Error updating subuser: " + e.getMessage());
        }
    }

    
    // Creates a main account JSON for a subuser and returns the new UID
    private String createSubUserMainAccount(String username, String password, String parentUid) {
        try {
            File folder = new File("MainAccounts");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            ObjectWriter writer = myMapper.writerWithDefaultPrettyPrinter();
            File[] files = folder.listFiles();
            
            int newHigh = 0;
            int newID = 0;
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        User tempUser = myMapper.readValue(f, User.class);
                        int currentHigh = Integer.parseInt(tempUser.mainFileNum);
                        int currentID = Integer.parseInt(tempUser.uid);
                        if (currentHigh > newHigh) {
                            newHigh = currentHigh;
                        }
                        if (currentID > newID) {
                            newID = currentID;
                        }
                    }
                }
            }
            
            String newFileNum = incrementCounter(String.valueOf(newHigh));
            String newUID = incrementCounter(String.valueOf(newID));
            
            User subUser = new User(username, password, newUID, newFileNum);
            
            // Mark as sub and link to parent
            subUser.isSub = true;
            
            File newFile = new File(folder, newFileNum + ".json");
            writer.writeValue(newFile, subUser);
            
            // Create vault folder for subuser
            File subVault = new File("Accounts/" + newUID);
            if (!subVault.exists()) {
                subVault.mkdirs();
            }
            
            return newUID;
            
        } catch (Exception e) {
            System.out.println("Error creating new subuser account");
            System.out.println(e);
            return null;
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
                    if (f.isFile()) {
                        User temp = myMapper.readValue(f, User.class);
                        int uid = Integer.parseInt(temp.uid);
                        if (uid > maxUid) maxUid = uid;
                    }
                }
            }
            return String.valueOf(maxUid + 1);
            
        } catch (Exception e) {
            System.out.println("Error generating UID");
            return "1";
        }
    }
    
    private String generateNewMainFileNum() {
        try {
            File folder = new File ("MainAccounts");
            if (!folder.exists()) folder.mkdirs();
            
            int maxFileNum = 0;
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        User temp = myMapper.readValue(f, User.class);
                        int num = Integer.parseInt(temp.mainFileNum);
                        if (num > maxFileNum) maxFileNum = num;
                    }
                }
            }
            return String.valueOf(maxFileNum + 1);
            
        } catch (Exception e) {
            System.out.println("Error generating mainFuleNum");
            return "1";
        }
    }

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
    
    
}
