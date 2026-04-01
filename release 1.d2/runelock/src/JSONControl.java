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
            File folder = new File("Accounts");
            if (!folder.exists()){//If there are no account files
                folder.mkdirs();
            }
            File[] files = folder.listFiles();
            if (files != null){
                
                for (File f : files){
                    if (f.isFile()){
                        Account account = myMapper.readValue(f, Account.class);
                        this.user.accounts.add(account);
                        System.out.println("This is the account fileName: " + account.getFileNumber() + ".json");
                    } 
                }
            }
        } catch (Exception e){
            System.out.println("There was an error loading accounts");
            System.out.println(e);
        }
    }
    
    //Method to load userSubaccount file
    public void loadSubFiles(){
        try{
            File folder = new File("SubAccounts");
            if (!folder.exists()){
                folder.mkdirs();
            }
            File[] files = folder.listFiles();
            if (files != null){
                for (File f : files){
                    if (f.isFile()){
                        SubUser subUser = myMapper.readValue(f, SubUser.class);
                        this.user.subAccounts.add(subUser);
                        System.out.println("This is the name of sub account: " + subUser.getName());
                    }
                }
            }
        }catch(Exception e){
            System.out.println("There was an error loading sub accounts");
            System.out.println(e);
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
            
            String folderPath = "Accounts/";
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
    public void storeSubAccount(String aName, SubUser aUser){
        try{
            String folderPath = "SubAccounts/";
            File folder = new File(folderPath);
            if (!folder.exists()){
                folder.mkdirs();
            }
            SubUser tempUser = null;
            Boolean check = false;//For use with updating or creating JSON file
            for (SubUser user : this.user.subAccounts){//Search for user name in files
                if (user.getName().equals(aName)){
                    tempUser = user;
                    check = true;
                    break;
                }
            }
            if (check){//If subuser exists in user.subaccounts
                File currentFile = new File(folder, "Sub" + tempUser.fileNumber + ".json");
                System.out.println("Writing to: " + currentFile.getAbsolutePath());//testing for where currentFile is being saved
                System.out.println("This is current account file num: " + this.user.subFileNum);
                
                ObjectWriter writer = myMapper.writerWithDefaultPrettyPrinter();
                writer.writeValue(currentFile, tempUser);
                System.out.println("Success updating sub account");
            }else{//Not a subuser yet and needs main account file
                tempUser = aUser;
                File currentFile = new File(folder, "Sub" + this.user.subFileNum + ".json");
                System.out.println("Writing to: " + currentFile.getAbsolutePath());//testing for where currentFile is being saved
                System.out.println("This is current account file num: " + this.user.subFileNum);
                tempUser.setFileNumber(this.user.subFileNum);
                ObjectWriter writer = myMapper.writerWithDefaultPrettyPrinter();
                writer.writeValue(currentFile, tempUser);
                this.user.subFileNum = incrementCounter(this.user.subFileNum);//Turn back into string
                storeMainAccount(this.user.username, this.user.password);//Method call to update user mainfile
                createMainAccount(tempUser.username, tempUser.password);
                System.out.println("Success updating sub account");
            }
        } catch(Exception e){
            System.out.println("There was an error saving sub account info");
            System.out.println(e);
        }
    }
    
    //Method to store mainAccount files
    public void storeMainAccount(String aUsername, String aPassword){

        try{

            File folder = new File("MainAccounts");
            if (!folder.exists()){//If there are no main accounts make the folder for use later
                folder.mkdirs();
            }
            ObjectWriter writer = myMapper.writerWithDefaultPrettyPrinter();
            File[] files = folder.listFiles();
            if (files != null){//If files exist
                User tempUser;
            
                for (File f : files){
                    if (f.isFile()){
                        tempUser = myMapper.readValue(f, User.class);
                        if (aUsername.equals(tempUser.username)){
                            if (aPassword.equals(tempUser.password)){//If username and password exist in a file
                                System.out.println("Writing to: " + f.getAbsolutePath());//testing for where currentFile is being saved
                                writer.writeValue(f, this.user);
                            }else{//If username and password don't exist in a file
                                File newFile = new File(folder, this.user.mainFileNum + ".json");
                                System.out.println("Writing to: " + newFile.getAbsolutePath());
                                writer.writeValue(newFile, this.user);
                            }
                        }
                    }
                }
            }else{//If there are no files for main accounts
                File newFile = new File(folder, this.user.mainFileNum + ".json");
                System.out.println("Writing to: " + newFile.getAbsolutePath());
                writer.writeValue(newFile, this.user);
                this.user.mainFileNum = incrementCounter(this.user.mainFileNum);//Incremement value
            }    
            
        }catch (Exception e){
            System.out.println("There was an error saving main account info");
            System.out.println(e);
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
                User newUser = new User(aUsername, aPassword, true, newUID, newFileNum);
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
            String filePath = "Accounts/" + updated.getFileNumber() + ".json";
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), updated);
            System.out.println("Account updated: " + filePath);
        } catch (Exception e) {
            System.out.println("Error updating account" + e.getMessage());
        }
    }
    
    // Method to delete JSON file containing select login credentials
    public void deleteAccount(Account target) {
        try {
            String filePath = "Accounts/" + target.getFileNumber() + "json";
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
    public void deleteSubUser(SubUser target) {
        // Delete JSON file from subAccounts
    try {
        String filePath = "SubAccounts/Sub" + target.getfileNumber() + ".json";
        File file = new File(filePath);

        if (file.exists()) {
            file.delete();
            System.out.println("SubUser deleted: " + filePath);
        }

        user.getsubAccounts().remove(target);
        storeMainAccount(user.getUsername(), user.getPassword());

    } catch (Exception e) {
        System.out.println("Error deleting subuser: " + e.getMessage());
    }
}
    
    // Method to update subuser information
    public void updateSubUser(SubUser updated) {
    try {
        String filePath = "SubAccounts/Sub" + updated.getfileNumber() + ".json";
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), updated);

        System.out.println("SubUser updated: " + filePath);

        // Update list of sub user accounts
        for (int i = 0; i < user.getsubAccounts().size(); i++) {
            if (user.getsubAccounts().get(i).getfileNumber().equals(updated.getfileNumber())) {
                user.getsubAccounts().set(i, updated);
                break;
            }
        }

        // Saves updated JSON from main account
        storeMainAccount(user.getUsername(), user.getPassword());

    } catch (Exception e) {
        System.out.println("Error updating subuser: " + e.getMessage());
    }
}


    
    //Method to store settings
    
    //Method to read settings
    
    //Methods for deleting things
    
    //Method to check if username/password exist in accounts file
    
    
}
