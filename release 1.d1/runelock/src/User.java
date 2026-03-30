/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;
/**
 *
 * @author ckurd
 */
public class User {
    String username;
    String password;
    @JsonProperty("sub")
    Boolean isSub;
    @JsonProperty("subAccounts")
    ArrayList<ArrayList> tempSubAccounts;
    @JsonProperty("UUID")
    String uID;
    ArrayList accounts = new ArrayList();
    ArrayList<SubUser> subAccounts = new ArrayList();
    
    
    public String getUsername(){
        return this.username;
    }
    
    public String getPassword(){
        return this.password;
    }
    
    public Boolean getIsSub(){
        return this.isSub;
    }
    
    public ArrayList getTempSubAccounts(){
        return this.tempSubAccounts;
    }
    
    public String getUID(){
        return this.uID;
    }
    
    public ArrayList getAccounts(){
        return this.accounts;
    }
    
    public ArrayList getSubaccounts(){
        return this.subAccounts;
    }
}
