/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
/**
 *
 * @author ckurd
 */
public class User {
    @JsonProperty("username")
    String username;
    @JsonProperty("password")
    String password;
    @JsonProperty("sub")
    Boolean isSub;
    
    @JsonProperty("subAccounts")
    ArrayList<SubUser> subAccounts = new ArrayList();
    
    @JsonProperty("uid")
    String uid;
    
    @JsonProperty("subFileNum")
    String subFileNum;
    @JsonProperty("accountFileNum")
    String accountFileNum;
    @JsonProperty("mainFileNum")
    String mainFileNum;
    
    @JsonIgnore
    ArrayList accounts = new ArrayList();//Change to set?
    
    public User(){
        
    }
    
    public User(String username, String aPassword, Boolean sub, String uuid, String fileNum){
        this.username = username;
        this.password = aPassword;
        this.isSub = sub;
        this.uid = uuid;
        this.subFileNum = "0";
        this.accountFileNum = "0";
        this.mainFileNum = fileNum;
        this.subAccounts = new ArrayList();
        this.accounts = new ArrayList();
    }
    
    
    public String getUsername(){
        return this.username;
    }
    
    public String getPassword(){
        return this.password;
    }
    
    public Boolean getIsSub(){
        return this.isSub;
    }
   
    
    public String getuid(){
        return this.uid;
    }
    
    public ArrayList getAccounts(){
        return this.accounts;
    }
    
    public ArrayList<SubUser> getsubAccounts(){
        return this.subAccounts;
    }
    
    public String getSubFileNum(){
        return this.subFileNum;
    }
    
    public String getAccountFileNum(){
        return this.accountFileNum;
    }
    
    public String getmainFileNum(){
        return this.mainFileNum;
    }
}
