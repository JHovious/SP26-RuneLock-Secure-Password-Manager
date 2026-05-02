/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.model;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import model.Security;
import model.User;
import model.Account;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.LocalDate;


/**
 *
 * @author ckurd
 */
public class SecurityTest {
    private Security testObject;
    private ObjectMapper myMapper;
    private User mainUser;
    private User subUser;
    private Account account;
    
    @Before
    public void setUP(){
        myMapper = new ObjectMapper();
        myMapper.registerModule(new JavaTimeModule());
        myMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String username = "username";
        String password = "12345";
        String fileNum = "1";
        mainUser = new User(username, password, fileNum, fileNum);
        subUser = new User(username, password, fileNum, fileNum, "pUID");
        try{
            testObject = new Security(username, password, fileNum);
            assertTrue(true);
        }catch(Exception e){
            System.out.println(e);
            fail();
        }
        String url = "facebook.com";
        ArrayList<String> tags = new ArrayList<>(Arrays.asList("education", "finance"));
        LocalDate aCreated = LocalDate.of(2024, 3, 15);
        LocalDate lastUsedTime = LocalDate.of(2024, 3, 15);
        account = new Account(url, username, password, tags, aCreated, lastUsedTime, fileNum);

    }
    
    /**
     *=====================
     * Encryption Tests
     * =====================
     */
    
    @Test//Test with a user object to encrypt ensuring the string value of the object is not the same as encrypted value
    public void testEncryptionUser(){
        Boolean check = false;
        String value = "";
        String encryptedString = ""; 
        try{
            value = myMapper.writeValueAsString(this.mainUser);
            byte[] encryptedUser = testObject.encrypt(value.getBytes(StandardCharsets.UTF_8));
            check = true;
            encryptedString = Base64.getEncoder().encodeToString(encryptedUser);
            System.out.println("This is the encrypted string: " + encryptedString);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if (check){
                assertNotEquals(encryptedString, value);
            }else{
                fail();
            }
        }
    }
    
    @Test//Test with a subuser object to encrypt ensuring the string value of the object is not the same as encrypted value
    public void testEncryptionSub(){
        Boolean check = false;
        String value = "";
        String encryptedString = ""; 
        try{
            value = myMapper.writeValueAsString(this.subUser);
            byte[] encryptedUser = testObject.encrypt(value.getBytes(StandardCharsets.UTF_8));
            check = true;
            encryptedString = Base64.getEncoder().encodeToString(encryptedUser);
            System.out.println("This is the encrypted string: " + encryptedString);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if (check){
                assertNotEquals(encryptedString, value);
            }else{
                fail();
            }
        }
    }
    
    @Test//Test with a account object to encrypt ensuring the string value of the object is not the same as encrypted value
    public void testEncryptionAccount(){
        Boolean check = false;
        String value = "";
        String encryptedString = ""; 
        try{
            value = myMapper.writeValueAsString(this.account);
            byte[] encryptedAccount = testObject.encrypt(value.getBytes(StandardCharsets.UTF_8));
            check = true;
            encryptedString = Base64.getEncoder().encodeToString(encryptedAccount);
            System.out.println("This is the encrypted string: " + encryptedString);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if (check){
                assertNotEquals(encryptedString, value);
            }else{
                fail("Exception was thrown");
            }
        }
    }
    
    
    /**
     *=====================
     * Decryption Tests
     * =====================
     */
    
    @Test//Test with a byte to decrypt ensuring the string is readable text and not encrypted
    public void testDecryptionUser(){
        Boolean check = false;
        String value = "";
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try{
            value = mapper.writeValueAsString(this.mainUser);
            byte[] encryptedUser = testObject.encrypt(value.getBytes(StandardCharsets.UTF_8));
            User thisUser = null;
            try{
                byte[] plainUser = testObject.decrypt(encryptedUser);
                String jsonObject = new String(plainUser, StandardCharsets.UTF_8);
                thisUser = mapper.readValue(jsonObject, User.class);
                check = true;
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                if (check){
                    if (thisUser.getUsername().equals(this.mainUser.getUsername()) && thisUser.getPassword().equals(this.mainUser.getPassword())
                            && thisUser.getPastUsername().equals(this.mainUser.getPastUsername()) && thisUser.getPastPassword().equals(this.mainUser.getPastPassword())
                            && thisUser.getuid().equals(this.mainUser.getuid()) && thisUser.getIsSub().equals(this.mainUser.getIsSub())
                            && thisUser.getSubFileNum().equals(this.mainUser.getSubFileNum())&& thisUser.getAccountFileNum().equals(this.mainUser.getAccountFileNum()) 
                            && thisUser.getMainFileNum().equals(this.mainUser.getMainFileNum())&& thisUser.getSubAccounts().equals(this.mainUser.getSubAccounts()) 
                            && thisUser.getAccounts().equals(this.mainUser.getAccounts())&& thisUser.getVaultVersion() == this.mainUser.getVaultVersion()){
                    //If the user matches the decrypted user
                        assertTrue(true);
                    }else{
                        fail();
                    }
                }else{
                    fail("Exception was thrown1");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            fail("Exception was thrown2");
        }
    }
    
    
    @Test//Test with a byte to decrypt ensuring the string is readable text and not encrypted
    public void testDecryptionSub(){
        Boolean check = false;
        String value = "";
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try{
            value = mapper.writeValueAsString(this.subUser);
            byte[] encryptedUser = testObject.encrypt(value.getBytes(StandardCharsets.UTF_8));
            User thisUser = null;
            try{
                byte[] plainUser = testObject.decrypt(encryptedUser);
                String jsonObject = new String(plainUser, StandardCharsets.UTF_8);
                thisUser = mapper.readValue(jsonObject, User.class);
                check = true;
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                if (check){
                    if (thisUser.getUsername().equals(this.subUser.getUsername()) && thisUser.getPassword().equals(this.subUser.getPassword())
                            && thisUser.getPastUsername().equals(this.subUser.getPastUsername()) && thisUser.getPastPassword().equals(this.subUser.getPastPassword())
                            && thisUser.getuid().equals(this.subUser.getuid()) && thisUser.getIsSub().equals(this.subUser.getIsSub())
                            && thisUser.getSubFileNum().equals(this.subUser.getSubFileNum())&& thisUser.getAccountFileNum().equals(this.subUser.getAccountFileNum()) 
                            && thisUser.getMainFileNum().equals(this.subUser.getMainFileNum())&& thisUser.getSubAccounts().equals(this.subUser.getSubAccounts()) 
                            && thisUser.getAccounts().equals(this.subUser.getAccounts())&& thisUser.getVaultVersion() == this.subUser.getVaultVersion()){
                    //If the user matches the decrypted subuser
                        assertTrue(true);
                    }else{
                        fail();
                    }
                }else{
                    fail("Exception was thrown1");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            fail("Exception was thrown2");
        }
    }
    
    
    @Test//Test with a byte to decrypt ensuring the string is readable text and not encrypted
    public void testDecryptionAccount(){
        Boolean check = false;
        String value = "";
        try{
            value = myMapper.writeValueAsString(this.account);
            byte[] encryptedAccount = testObject.encrypt(value.getBytes(StandardCharsets.UTF_8));
            Account thisAccount = null;
            try{
                byte[] plainUser = testObject.decrypt(encryptedAccount);
                String jsonObject = new String(plainUser, StandardCharsets.UTF_8);
                thisAccount = myMapper.readValue(jsonObject, Account.class);
                check = true;
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                if (check){
                    if (thisAccount.getUrl().equals(this.account.getUrl()) && thisAccount.getUsername().equals(this.account.getUsername()) 
                            && thisAccount.getPassword().equals(this.account.getPassword()) && thisAccount.getTags().equals(this.account.getTags())
                            && thisAccount.getCreated().equals(this.account.getCreated()) && thisAccount.getLastUsed().equals(this.account.getLastUsed())
                            && thisAccount.getFileNumber().equals(this.account.getFileNumber())){
                    //If the user matches the decrypted user
                        assertTrue(true);
                    }else{
                        fail();
                    }    
                }else{
                    fail("Exception was thrown");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            fail("Exception was thrown");
        }
    }
    

    
}
