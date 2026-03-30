/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
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
    @JsonFormat(pattern = "yyy-MM-dd")
    LocalDate createdDate;
    @JsonProperty("lastUsed")
    @JsonFormat(pattern = "yyy-MM-dd")
    LocalDate lastUsed;
    String fileNumber;
    
    public Account(){
        url = "";
        username = "";
        password = "";
        tags = new ArrayList();
        createdDate = null;
        lastUsed = null;
        fileNumber = "99000";
    }
    
    public Account(String aURL, String aUsername, String aPassword, ArrayList someTags, LocalDate aCreated, LocalDate lastUsedTime, String fileName){
        this.url = aURL;
        this.username = aUsername;
        this.password = aPassword;
        this.tags = someTags;
        this.createdDate = aCreated;
        this.lastUsed = lastUsedTime;
        this.fileNumber = fileName;
    }
    
    
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
    
    public LocalDate getCreatedDate(){
        return this.createdDate;
    }
    
    public LocalDate getLastUsed(){
        return this.lastUsed;
    }
    
    public String getFileNumber(){
        return this.fileNumber;
    }
    
}
