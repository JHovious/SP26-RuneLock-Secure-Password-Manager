import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.io.InputStream;


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
    }
    
    //Method to verify initial login
    public Boolean verifyLogin(String username, String password){
        loadMainFile();
        if (this.check){
            Boolean newCheck;
            newCheck = this.securityObject.verifyLogin(username, password);
            if (newCheck){
                for (ArrayList<String> account : this.user.tempSubAccounts){//Need to create subUser objects
                    SubUser newSub = new SubUser(account.get(1), account.get(2), account.get(0));
                    this.user.subAccounts.add(newSub);//Add sub object to user's array list of objects
                }
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
                        System.out.println("This is the account url: " + account.getURL());
                    } 
                }
            }
        } catch (Exception e){
            System.out.println("There was an error loading accounts");
            System.out.println(e);
        }
    }
    
    //Method to load userSubaccount file
    
    
    //Method to store account files
    
    //Method to store sub account files
    
    //Method to store mainAccount files
    
    //Method to store settings
    
    //Method to read settings
    
    //Method to check if username/password exist in accounts file
    
}
