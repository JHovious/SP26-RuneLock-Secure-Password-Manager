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
 * @author ckurd
 */
public class JSONControl {
    private User user;//Used for storing mainFile object info
    private Boolean check;//Used if file loaded successfully
    private Security securityObject;
    private ObjectMapper myMapper;
    
    
    //Constructor
    public JSONControl(){
        this.user = null;
        this.check = false;
        this.securityObject = new Security();
        this. myMapper = new ObjectMapper();
        myMapper.registerModule(new JavaTimeModule());
        myMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    }
    
    //Method to verify initial login
    public Boolean verifyLogin(String username, String password){
        loadMainFile();
        if (this.check){
            Boolean newCheck;
            newCheck = this.securityObject.verifyLogin(username, password);
            if (newCheck){
                return newCheck;
            } else{
                return false;
            }
        }
        return false;
    }
    
    //Method to load the main id check file
    public void loadMainFile(){
        try{
            InputStream in = getClass().getResourceAsStream("/mainAccounts/0.json");
            this.user = myMapper.readValue(in, User.class);
            
            this.check = true;
        }catch (Exception e){
            System.out.println("There was an error loading mainFile.json");
            System.out.println(e);
            this.check = false;
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
            File folder = new File("Subaccounts");
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
            storeMainAccount();//Method call to update user mainfile
            System.out.println("Success adding account");
            
        }catch (Exception e){
            System.out.println("There was an error saving account info");
            System.out.println(e);
        }
    }
    
    //Method to store sub account files !!!!!!!!UNTESTED!!!!!!!!!
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
            if (check){
                File currentFile = new File(folder, "Sub" + tempUser.fileNumber + ".json");
                System.out.println("Writing to: " + currentFile.getAbsolutePath());//testing for where currentFile is being saved
                System.out.println("This is current account file num: " + this.user.subFileNum);
                
                ObjectWriter writer = myMapper.writerWithDefaultPrettyPrinter();
                writer.writeValue(currentFile, tempUser);
                System.out.println("Success updating sub account");
            }else{
                tempUser = aUser;
                File currentFile = new File(folder, "Sub" + this.user.subFileNum + ".json");
                System.out.println("Writing to: " + currentFile.getAbsolutePath());//testing for where currentFile is being saved
                System.out.println("This is current account file num: " + this.user.subFileNum);
                tempUser.setFileNumber(this.user.subFileNum);
                ObjectWriter writer = myMapper.writerWithDefaultPrettyPrinter();
                writer.writeValue(currentFile, tempUser);
                int fileNumber = Integer.parseInt(this.user.subFileNum);//Turn into integer
                fileNumber += 1;
                this.user.subFileNum = String.valueOf(fileNumber);//Turn back into string
                storeMainAccount();//Method call to update user mainfile
                System.out.println("Success updating sub account");
            }
        } catch(Exception e){
            System.out.println("There was an error saving sub account info");
            System.out.println(e);
        }
    }
    
    //Method to store mainAccount files
    public void storeMainAccount(){

        try{

            String folderPath = "build/classes/mainAccounts/";
            File folder = new File(folderPath);

            File currentFile = new File(folder, "0.json");
            System.out.println("Writing to: " + currentFile.getAbsolutePath());//testing for where currentFile is being saved
            
            ObjectWriter writer = myMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(currentFile, this.user);
            
            
        }catch (Exception e){
            System.out.println("There was an error saving main account info");
            System.out.println(e);
        }
    }
    
    //Method to store settings
    
    //Method to read settings
    
    //Method to check if username/password exist in accounts file
    
    
}
