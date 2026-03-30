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
public class Account {
    
    String url;
    String username;
    String password;
    ArrayList tags;
    @JsonProperty("created")
    String createdDate;
    String lastUsed;
    
    
    public String getURL(){
        return this.url;
    }
    
    public String getUsername(){
        return this.username;
    }
    
    public String getPassword(){
        return this.password;
    }
    
    public ArrayList getTags(){
        return this.tags;
    }
    
    public String getCreatedDate(){
        return this.createdDate;
    }
    
    public String getLastUsed(){
        return this.lastUsed;
    }
    
}
