/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
/**
 * This class handles all constructors, getters, and setters needed for
 * managing Users in JSONControl.java
 * @author ckurd
 */
public class User {
    @JsonProperty("username")
    String username;
    @JsonProperty("password")
    String password;
    @JsonProperty("sub")
    Boolean isSub;
    @JsonProperty("parentUid")
    String parentUid;
    
    @JsonProperty("subAccounts")
    ArrayList<User.SubUserMeta> subAccounts = new ArrayList<>();
    
    @JsonProperty("uid")
    String uid;
    
    @JsonProperty("subFileNum")
    String subFileNum;
    @JsonProperty("accountFileNum")
    String accountFileNum;
    @JsonProperty("mainFileNum")
    String mainFileNum;
    
    @JsonIgnore
    ArrayList<Account> accounts = new ArrayList<>();
    
    public User(){
        
    }
    // Main User
    public User(String username, String password, String uid, String fileNum){
        this.username = username;
        this.password = password;
        this.uid = uid;
        this.isSub = false;
        this.parentUid = null;
        this.subFileNum = "0";
        this.accountFileNum = "0";
        this.mainFileNum = fileNum;
        this.subAccounts = new ArrayList<>();
        this.accounts = new ArrayList<>();
    }
    // Sub User
    public User(String username, String password, String uid, String fileNum, String parentUid) {
        this.username = username;
        this.password = password;
        this.uid = uid;
        this.isSub = true;
        this.parentUid = parentUid;
        this.subFileNum = "0";
        this.accountFileNum = "0";
        this.mainFileNum = fileNum;
        this.subAccounts = new ArrayList<>();
        this.accounts = new ArrayList<>();
    }
    
    public static class SubUserMeta {
        @JsonProperty("uid")
        public String uid;
        
        @JsonProperty("username")
        public String username;
        
        @JsonProperty("name")
        public String name;
        
        public SubUserMeta() {}
        
        public SubUserMeta(String uid, String username, String name) {
            this.uid = uid;
            this.username = username;
            this.name = name;
        }
    }
    
    public boolean isSub() {
        return Boolean.TRUE.equals(this.isSub);
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
    
    public ArrayList<Account> getAccounts(){
        return this.accounts;
    }
    
    public ArrayList<SubUserMeta> getsubAccounts(){
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
