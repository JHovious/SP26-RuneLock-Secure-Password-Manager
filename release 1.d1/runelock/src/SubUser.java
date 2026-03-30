/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author ckurd
 */
public class SubUser {
    private String username;
    private String password;
    private String name;
    
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
}
