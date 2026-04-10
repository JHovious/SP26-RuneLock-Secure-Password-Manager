/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

/**
 * This class is meant to create all variables, getters, and setters
 * needed between the JSON database and displaying the info in the 
 * mainWindow.java view.
 * @author ckurd, jahov
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {
    // Variable initialization

    String url;
    String username;
    String password;
    ArrayList<String> tags;

    @JsonProperty("created")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate createdDate;

    String fileNumber;
    
    @JsonIgnore
    String ownerUid;


    
    // Constructors 
    public Account() {
        url = "";
        username = "";
        password = "";
        tags = new ArrayList<>();
        createdDate = null;
        fileNumber = "99000";
    }

    
    public Account(String aURL, String aUsername, String aPassword,
                   ArrayList<String> someTags, LocalDate aCreated,
                   LocalDate lastUsedTime, String fileName) {

        this.url = aURL;
        this.username = aUsername;
        this.password = aPassword;
        this.tags = someTags;
        this.createdDate = aCreated;
        this.fileNumber = fileName;
    }

    
    // Getters
    public String getOwnerUid() {
        return ownerUid;
    }
    
    public String getUrl() {
        return this.url;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public ArrayList<String> getTags() {
        return this.tags;
    }

    public LocalDate getCreated() {
        return this.createdDate;
    }

    public String getFileNumber() {
        return this.fileNumber;
    }

    // Setters
    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public void setCreated(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public void setFileNumber(String fileNumber) {
        this.fileNumber = fileNumber;
    }
}
