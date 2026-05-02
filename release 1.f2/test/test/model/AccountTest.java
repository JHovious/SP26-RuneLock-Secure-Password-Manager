/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import model.Account;


/**
 *
 * @author ckurd
 */
public class AccountTest {
    private Account testObject;
    String username;
    String password;
    String fileNum;
    String url;
    ArrayList<String> tags;
    LocalDate aCreated;
    LocalDate lastUsedTime;
    
    
    @Before
    public void setUP(){
        username = "username";
        password = "12345";
        fileNum = "1";
        url = "facebook.com";
        tags = new ArrayList<>(Arrays.asList("education", "finance"));
        aCreated = LocalDate.of(2026, 4, 30);
        lastUsedTime = LocalDate.of(2024, 4, 30);
        testObject = new Account(url, username, password, tags, aCreated, lastUsedTime, fileNum);
    }
    
    /**
     *=====================
     * Constructor Tests
     * =====================
     */
    
    @Test
    public void testGetOwnerUid() {
        String temp = "test";
        testObject.setOwnerUid(temp);
        assertEquals(testObject.getOwnerUid(), temp);
    }
    
    @Test
    public void testGetUrl() {
        assertEquals(testObject.getUrl(), this.url);
    }

    @Test
    public void testGetUsername() {
        assertEquals(testObject.getUsername(), this.username);
    }

    @Test
    public void testGetPassword() {
        assertEquals(testObject.getPassword(), this.password);
    }

    @Test
    public void testGetTags() {
        assertEquals(testObject.getTags(), this.tags);
    }

    @Test
    public void testGetCreated() {
        assertEquals(testObject.getCreated(), this.aCreated);
    }
    
    @Test
    public void testGetLastUsed() {
        assertEquals(testObject.getLastUsed(), this.lastUsedTime);
    }

    @Test
    public void testGetFileNumber() {
        assertEquals(testObject.getFileNumber(), this.fileNum);
    }

    @Test
    public void testSetOwnerUid() {
        String ownerUid = "9";
        testObject.setOwnerUid(ownerUid);
        assertEquals(testObject.getOwnerUid(), ownerUid);
    }
    
    @Test
    public void testSetUrl() {
        String url = "facebook.com";
        testObject.setOwnerUid(url);
        assertEquals(testObject.getUrl(), url);
    }

    @Test
    public void testSetUsername() {
        String username = "test";
        testObject.setUsername(username);
        assertEquals(testObject.getUsername(), username);
    }

    @Test
    public void testSetPassword() {
        String password = "test";
        testObject.setPassword(password);
        assertEquals(testObject.getPassword(), password);
    }

    @Test
    public void testSetTags() {
        ArrayList<String> tags = new ArrayList<>(Arrays.asList("test1", "test2"));
        testObject.setTags(tags);
        assertEquals(testObject.getTags(), tags);
    }

    @Test
    public void testSetCreated() {
        LocalDate createdDate = LocalDate.of(2026, 5, 1);
        testObject.setCreated(createdDate);
        assertEquals(testObject.getCreated(), createdDate);
   }
    
    @Test
    public void testSetLastUsed() {
       LocalDate lastUsed = LocalDate.of(2026, 5, 1);
       testObject.setLastUsed(lastUsed);
       assertEquals(testObject.getLastUsed(), lastUsed);
    }

    @Test
    public void testSetFileNumber() {
        String fileNumber = "test";
        testObject.setFileNumber(fileNumber);
        assertEquals(testObject.getFileNumber(), fileNumber);
    }
}
