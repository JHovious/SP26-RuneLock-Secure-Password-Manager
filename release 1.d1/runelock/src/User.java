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
    
    @JsonIgnore
    ArrayList accounts = new ArrayList();//Change to set?
    
    
    
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
}
