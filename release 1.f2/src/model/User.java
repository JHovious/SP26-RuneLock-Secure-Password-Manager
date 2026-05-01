package model;

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
    
    @JsonProperty("pastUsername")
    String pastUsername;
    
    @JsonProperty("pastPassword")
    String pastPassword;
    
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
    
    @JsonProperty("vaultVersion")
    int vaultVersion;
    
    @JsonProperty("authorizedDevices")
    ArrayList<String> authorizedDevices = new ArrayList<>();
    
    @JsonIgnore
    ArrayList<Account> accounts = new ArrayList<>();
    
    public User(){
        
    }
    // Main User
    public User(String username, String password, String uid, String fileNum){
        this.username = username;
        this.password = password;
        this.pastUsername = username;
        this.pastPassword = password;
        this.uid = uid;
        this.isSub = false;
        this.parentUid = null;
        this.subFileNum = "0";
        this.accountFileNum = "0";
        this.mainFileNum = fileNum;
        this.subAccounts = new ArrayList<>();
        this.accounts = new ArrayList<>();
        this.vaultVersion = 0;
        
    }
    // Sub User
    public User(String username, String password, String uid, String fileNum, String parentUid) {
        this.username = username;
        this.password = password;
        this.pastUsername = username;
        this.pastPassword = password;
        this.uid = uid;
        this.isSub = true;
        this.parentUid = parentUid;
        this.subFileNum = "0";
        this.accountFileNum = "0";
        this.mainFileNum = fileNum;
        this.subAccounts = new ArrayList<>();
        this.accounts = new ArrayList<>();
        this.vaultVersion = 0;
    }
    
    public String getPastUsername() { 
        return pastUsername; 
    }
    
    public String getPastPassword() { 
        return pastPassword; 
    }

    public void setPastUsername(String pastUsername) { 
        this.pastUsername = pastUsername; 
    }
    
    public void setPastPassword(String pastPassword) { 
        this.pastPassword = pastPassword; 
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setParentUid(String parentUid) {
        this.parentUid = parentUid;
    }
    
    public void setSubFileNum(String subFileNum) {
        this.subFileNum = subFileNum;
    }
    
    public void setAccountFileNum(String accountFileNum) {
        this.accountFileNum = accountFileNum;
    }
    
    public void setMainFileNum(String mainFileNum) {
        this.mainFileNum = mainFileNum;
    }

    public int getVaultVersion() {
        return vaultVersion;
    }
    
    public void setVaultVersion(int vaultVersion) {
        this.vaultVersion = vaultVersion;
    }
    
    public void incrementVaultVersion() {
        this.vaultVersion++;
    }
    
    public ArrayList<String> getAuthorizedDevices() {
    return authorizedDevices;
    }

    public void authorizeDevice(String deviceId) {
        if (!authorizedDevices.contains(deviceId)) {
            authorizedDevices.add(deviceId);
        }
    }

    public boolean isDeviceAuthorized(String deviceId) {
        return authorizedDevices.contains(deviceId);
    }

    public void removeAuthorizedDevice(String deviceId) {
        authorizedDevices.remove(deviceId);
    }

    
    public static class SubUserMeta {
        @JsonProperty("uid")
        public String uid;
        
        @JsonProperty("username")
        public String username;
        
        @JsonProperty("name")
        public String name;
        
        @JsonProperty("encPassword")
        public String encPassword;
        
        public SubUserMeta() {}
        
        public SubUserMeta(String uid, String username, String name, String encPassword) {
            this.uid = uid;
            this.username = username;
            this.name = name;
            this.encPassword = encPassword;
        }
    }
    
    public boolean isSub() {
        return Boolean.TRUE.equals(this.isSub);
    }
    
    public String getUsername(){
        return this.username;
    }
    
    public String getParentUid() {
        return this.parentUid;
    }
    
    public String getPassword(){
        return this.password;
    }
    
    public Boolean getIsSub(){
        return this.isSub;
    }
    
    public void setIsSub(boolean isSub) {
        this.isSub = isSub;
    }
   
    
    public String getuid(){
        return this.uid;
    }
    
    public ArrayList<Account> getAccounts(){
        return this.accounts;
    }
    
    public ArrayList<SubUserMeta> getSubAccounts(){
        return this.subAccounts;
    }
    
    public String getSubFileNum(){
        return this.subFileNum;
    }
    
    public String getAccountFileNum(){
        return this.accountFileNum;
    }
    
    public String getMainFileNum(){
        return this.mainFileNum;
    }
}
