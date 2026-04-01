
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * This class handles all constructors, getters, and setters needed for
 * managing SubUser in JSONControl.java
 * @author ckurd
 */
public class SubUser {
    @JsonProperty("username")
    String username;
    @JsonProperty("password")
    String password;
    @JsonProperty("name")
    private String name;
    @JsonProperty("fileNumber")
    String fileNumber;
    
    public SubUser(){
        this.username = "";
        this.password = "";
        this.name = "";
    }
    
    public SubUser(String aUsername, String aPassword, String aName){
        this.username = aUsername;
        this.password = aPassword;
        this.name = aName;
    }
    
    public String getUsername(){
        return this.username;
    }
    
    public String getPassword(){
        return this.password;
    }
    
    public String getName(){
        return this.name;
    }
    
    public String getfileNumber(){
        return this.fileNumber;
    }
    public void setFileNumber(String aFileNumber){
        this.fileNumber = aFileNumber;
    }
    
    
}
