import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.util.ArrayList;
import java.io.InputStream;
import java.time.LocalDate;
import java.io.OutputStream;
import java.util.Random;
import java.io.FileNotFoundException;
import java.security.KeyStoreException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.DirectoryStream;


/**
 *
 * @author ckurd, jahov
 */
public class JSONControl {
    private User user;//Used for storing mainFile object info
    private int check;//Used if file loaded successfully
    
    private ObjectMapper myMapper;
    private int newFileNum;
   
    
    private Security cipherTool = null;
    
    
    //Constructor
    public JSONControl(){
        this.user = null;
        this.check = 0;
        
        this. myMapper = new ObjectMapper();
        myMapper.registerModule(new JavaTimeModule());
        myMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        newFileNum = 0;

    }
    
    public User getUser() {
        return this.user;
    }

    
    //Method to verify initial login
    public int verifyLogin(String username, String password){
        try{
            
            if (this.cipherTool == null){
                    this.cipherTool = new Security(username, password, "");
            }
            System.out.println("Testing good to here");
            this.loadEncryptedUser(username, password);//Initiate loading main user for verification

        }catch(Exception e){
            System.out.println(e);
            return 0;
        }
        
        /*if (this.user == null) {  need to reconsider this code
            return 0;
        }*/
        
        return this.check;
    }
    
    //Method to load user account encrypted files
    public void loadAccountFiles(){
        try{
            File folder = new File("Accounts");
            if (!folder.exists()){//If there are no account files
                folder.mkdirs();
            }
            File[] files = folder.listFiles();
            if (files != null){
                
                byte[] encryptedAccount;
                for (File f : files){
                    if (f.isFile()){
                        //Decrypt file
                        encryptedAccount = Files.readAllBytes(f.toPath());//Convert File to Path
                        byte[] cipherAccount = cipherTool.decrypt(encryptedAccount);
                        String jsonObject = this.convertToString(cipherAccount);//Convert to a string/Json
                        Account thisAccount = this.myMapper.readValue(jsonObject, Account.class);
                        
                        this.user.accounts.add(thisAccount);
                        System.out.println("This is the account fileName: " + thisAccount.getFileNumber() + ".enc");
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
                byte[] encryptedSubUser;
                for (File f : files){
                    if (f.isFile()){
                        //Decrypt file
                        encryptedSubUser = Files.readAllBytes(f.toPath());//Convert File to Path
                        byte[] cipherSubUser = cipherTool.decrypt(encryptedSubUser);
                        String jsonObject = this.convertToString(cipherSubUser);//Convert to a string/Json
                        SubUser thisSubUser = this.myMapper.readValue(jsonObject, SubUser.class);
                        
                        this.user.subAccounts.add(thisSubUser);
                        System.out.println("This is the name of sub account: " + thisSubUser.getName());
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
        this.user.accounts.add(newAccount);
        this.user.accountFileNum = incrementCounter(this.user.accountFileNum);
        saveEncryptedUser();
    }
    
    //Method to store account files
    public void storeAccountData(Account anAccount){
        try{
            
            String folderPath = "Accounts/";
            File folder = new File(folderPath);
            if (!folder.exists()){
                folder.mkdirs();
            }

            String jsonAccount = myMapper.writeValueAsString(anAccount);
            byte[] encrypted = cipherTool.encrypt(convertToBytes(jsonAccount));
            Path path = Paths.get("Accounts", this.user.accountFileNum + ".enc");//.enc can be anything, but default for encoded file
            Files.write(path, encrypted);
            
            saveEncryptedUser();//Method call to update user mainfile
            System.out.println("Success adding account");
            
        }catch (JsonProcessingException e){
            System.out.println(e);
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
                String jsonSubAccount = myMapper.writeValueAsString(tempUser);
                byte[] encrypted = cipherTool.encrypt(convertToBytes(jsonSubAccount));
                Path path = Paths.get("SubAccounts", tempUser.fileNumber + ".enc");//.enc can be anything, but default for encoded file
                Files.write(path, encrypted);
                System.out.println("Success updating sub account");
                
            }else{//Not a subuser yet and needs main account file
                aUser.setFileNumber(this.user.subFileNum);
                this.user.subFileNum = incrementCounter(this.user.subFileNum);//Turn back into string
                
                String jsonSubAccount = myMapper.writeValueAsString(aUser);
                byte[] encrypted = cipherTool.encrypt(convertToBytes(jsonSubAccount));
                Path path = Paths.get("SubAccounts", aUser.fileNumber + ".enc");//.enc can be anything, but default for encoded file
                Files.write(path, encrypted);
                this.user.subAccounts.add(aUser);
                
                createMainAccount(aUser.username, aUser.password); //Issue here with replacing logged in main account
                cipherTool.generateKeyStore(user);
                System.out.println("Success updating sub account");
            }
            saveEncryptedUser();//Method call to update user mainfile
            
        } catch(Exception e){
            System.out.println("There was an error saving sub account info");
            System.out.println(e);
        }
    }
    

    //Method to create main accounts
    public void createMainAccount(String aUsername, String aPassword){
        
        try{
            File folder = new File("MainAccounts");
            if (!folder.exists()){//If there are no main accounts make the folder for use later
                    folder.mkdirs();
            }
                //Generate UUID and use for fileName
            String newUID = generateUID();
            User newUser = new User(aUsername, aPassword, false, newUID, newUID, aUsername, aPassword);
            if (this.cipherTool == null){
                this.cipherTool = new Security(aUsername, aPassword, newUID);
            }
            try{
                String jsonUser = myMapper.writeValueAsString(newUser);
                byte[] encrypted = this.cipherTool.encrypt(convertToBytes(jsonUser));
                Path path = Paths.get("MainAccounts", newUser.mainFileNum + ".enc");//.enc can be anything, but default for encoded file
                Files.write(path, encrypted);
            }catch (JsonProcessingException e){
                System.out.println(e);
            }catch(Exception e){
                System.out.println(e);
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
        try{
            String jsonAccount = myMapper.writeValueAsString(updated);
            byte[] encrypted = cipherTool.encrypt(convertToBytes(jsonAccount));
            Path path = Paths.get("Accounts", updated.fileNumber + ".enc");//.enc can be anything, but default for encoded file
            Files.write(path, encrypted);

            saveEncryptedUser();
            
        }catch (JsonProcessingException e){
            System.out.println(e);
        }catch(Exception e){
            System.out.println(e);
        }
        
    }
    
    // Method to delete JSON file containing select login credentials
    public void deleteAccount(Account target) {
        try {
            String filePath = "Accounts/" + target.getFileNumber() + ".enc";
            File file = new File(filePath);
            
            if (file.exists()) {
                file.delete();
                System.out.println("Account deleted: " + filePath);
            }
            
            user.getAccounts().remove(target);
            saveEncryptedUser();
            
        } catch (Exception e) {
            System.out.println("Error deleting account: " + e.getMessage());
        }
    }
    
    // Method to remove subUser from main account
    public void deleteSubUser(SubUser target) {
        // Delete JSON file from subAccounts
    try {
        String filePath = "SubAccounts" + target.getfileNumber() + ".enc";
        File file = new File(filePath);

        if (file.exists()) {
            file.delete();
            System.out.println("SubUser deleted: " + filePath);
        }

        user.getsubAccounts().remove(target);
        saveEncryptedUser();

    } catch (Exception e) {
        System.out.println("Error deleting subuser: " + e.getMessage());
    }
}
    
    // Method to update subuser information
    public void updateSubUser(SubUser updated) {
    try {
        String jsonSubAccount = myMapper.writeValueAsString(updated);
        byte[] encrypted = cipherTool.encrypt(convertToBytes(jsonSubAccount));
        Path path = Paths.get("SubAccounts", updated.fileNumber + ".enc");//.enc can be anything, but default for encoded file
        Files.write(path, encrypted);

        saveEncryptedUser();

        System.out.println("SubUser updated: " + updated.fileNumber + ".enc");

        // Update list of sub user accounts
        for (int i = 0; i < user.getsubAccounts().size(); i++) {
            if (user.getsubAccounts().get(i).getfileNumber().equals(updated.getfileNumber())) {
                user.getsubAccounts().set(i, updated);
                break;
            }
        }

        // Saves updated JSON from main account
        saveEncryptedUser();

    } catch (Exception e) {
        System.out.println("Error updating subuser: " + e.getMessage());
    }
}

    //Method to create a UID for a user
    public String generateUID(){
        Random rand = new Random();
        int numID = rand.nextInt();
        String newUID = Integer.toString(numID);
        return newUID;
    }
    
    //Method to generate bytes for a string, like json or an object turned into a string
    public byte[] convertToBytes(String stringObject){
        byte[] plaintext = stringObject.getBytes(StandardCharsets.UTF_8);
        return plaintext;
    }
    
    //Method to convert a byte array into a string or json object
    public String convertToString(byte[] plainText){
        String newJSON = new String(plainText, StandardCharsets.UTF_8);
        return newJSON;
    }
    
    //Method to encrypt a main user file and save
    public void saveEncryptedUser(){
        try{
            String jsonUser = myMapper.writeValueAsString(this.user);
            byte[] encrypted = cipherTool.encrypt(convertToBytes(jsonUser));
            Path path = Paths.get("MainAccounts", this.user.mainFileNum + ".enc");//.enc can be anything, but default for encoded file
            Files.write(path, encrypted);
            
        }catch (JsonProcessingException e){
            System.out.println(e);
        }catch(Exception e){
            System.out.println(e);
        }
        
    }
    
    public void loadEncryptedUser(String aUsername, String aPassword){
        
        try{
            //Need to check if there is a keystore folder before doing anything else
            
            File folder = new File("MainAccounts");
            if (!folder.exists()){//If there are no main accounts make the folder for use later
                this.check = 2;//User needs to create a runelock account
                return;
            }
            //DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.enc");//Get access to all encoded files
            File[] files = folder.listFiles();
            
            byte[] encryptedUser;
            for(File file : files){
                encryptedUser = Files.readAllBytes(file.toPath());//Convert File to Path
                byte[] cipherUser = cipherTool.decrypt(encryptedUser);
                String jsonObject = this.convertToString(cipherUser);//Convert to a string/Json
                User thisUser = this.myMapper.readValue(jsonObject, User.class);
                if (thisUser.username.equals(aUsername) && thisUser.password.equals(aPassword)){
                    this.check = 1;
                    this.user = thisUser;
                    return;
                }
                
            }
            this.check = 0;
            this.user = null;
    
        }catch (Exception e){
            System.out.println(e);
        }
    }
    
    
    //Method to update keystore on runelock username/password updates
    public void handleUpdates(int whichUpdated){
        cipherTool.updateKeystore(this.user, whichUpdated);
        saveEncryptedUser();
    }
    
}
