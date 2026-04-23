
/**
 *
 * @author ckurd
 */

import java.io.File;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.KeyGenerator;
import java.security.SecureRandom;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileOutputStream;
import java.util.Arrays;


public class Security {
    private static final int KEY_SIZE = 256; //Bits of a key
    private static final int IV_SIZE = 12; //Initialization vector size
    private static final int TAG_SIZE = 128; //Bits for authentication tag length
    
    private SecretKey key;
    private final SecureRandom random = new SecureRandom();
    private KeyStore keyStore;
    
    
    
    public Security(String aUsername, String aPassword, String fileName) throws Exception{
        char[] password = aPassword.toCharArray();
        KeyStore ks = KeyStore.getInstance("PKCS12");

        File folder = new File("Security");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // The keystore file for THIS user
        File keystoreFile = new File("Security/" + fileName + ".p12");

        if (!keystoreFile.exists()) {
            // FIRST TIME USER (main or sub)
            ks.load(null, password);

            KeyGenerator newKey = KeyGenerator.getInstance("AES");
            newKey.init(KEY_SIZE);
            SecretKey actualNewKey = newKey.generateKey();
            this.key = actualNewKey;

            KeyStore.SecretKeyEntry keyToStore = new KeyStore.SecretKeyEntry(this.key);
            KeyStore.ProtectionParameter ksPassword = new KeyStore.PasswordProtection(password);
            ks.setEntry(aUsername, keyToStore, ksPassword);

            this.keyStore = ks;

            FileOutputStream out = new FileOutputStream(keystoreFile);
            this.keyStore.store(out, password);

        } else {
            // RETURNING USER
            ks.load(new FileInputStream(keystoreFile), password);

            SecretKey key = (SecretKey) ks.getKey(aUsername, password);
            if (key == null) {
                throw new Exception("Keystore exists but no key found for alias: " + aUsername);
            }

            this.key = new SecretKeySpec(key.getEncoded(), "AES");
            this.keyStore = ks;
        }
    }

    
    
    
    
    public SecretKey getKey(){
        return this.key;
    }
    
    
    //Method for encryption
    public byte[] encrypt(byte[] input) throws Exception{
        byte[] iv = new byte[IV_SIZE];
        this.random.nextBytes(iv);
        
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");//Parameter needed is requested transformation in algorithm/mode/padding format
        GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);//Use GCM to specify set of parameters for Cipher 
        cipher.init(Cipher.ENCRYPT_MODE, this.key, spec);//Initializes cipher to encrypion mode
        
        byte[] ciphertext = cipher.doFinal(input);//cipher encrypts the byte string
        
        byte[] outgoing = new byte[iv.length + ciphertext.length];//Get outgoing byte array
        System.arraycopy(iv, 0, outgoing, 0, iv.length);//Use to copy bytes from one byte array to another to prepend iv to outgoing
        System.arraycopy(ciphertext, 0, outgoing, iv.length, ciphertext.length);//This appends ciphertext to outgoing
        
        
        return outgoing;
    }
    
    
    
    
    //Method for decryption
    public byte[] decrypt(byte[] input) throws Exception {
        System.out.println("Encrypted file length = " + input.length);
        byte[] iv = Arrays.copyOfRange(input, 0, IV_SIZE);

        byte[] ciphertext = Arrays.copyOfRange(input, IV_SIZE, input.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");//Transformation type
        GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);//Use GCM to specify set of parameters for Cipher 
        cipher.init(Cipher.DECRYPT_MODE, this.key, spec);//Initialize to decryption mode

        return cipher.doFinal(ciphertext);//return what the cypher outputs from decryption
    }
    
    
    
    
    //Method to update keystore on runelock username/password updates
    public void updateKeystore(User user, int whichUpdated){
        char[] password = user.password.toCharArray();
        String oldUsername = user.pastUsername;
        char[] oldPassword = user.pastPassword.toCharArray();
        String username = user.username;
        try{
            String fileString = "Security/" + user.mainFileNum + ".p12";
            if (whichUpdated == 0){//Username was updated
                KeyStore.Entry entry = this.keyStore.getEntry(oldUsername, new KeyStore.PasswordProtection(password));
                this.keyStore.setEntry(username, entry, new KeyStore.PasswordProtection(password));
                this.keyStore.deleteEntry(oldUsername);
                try{
                    FileOutputStream outStream = new FileOutputStream(fileString);
                    this.keyStore.store(outStream, password);
                }catch(Exception e){
                    System.out.println(e);
                }
            }else{//password updated
                this.keyStore.load(new FileInputStream(fileString), oldPassword);
                try{
                    FileOutputStream outStream = new FileOutputStream(fileString);
                    this.keyStore.store(outStream, password);
                }catch (Exception e){
                    System.out.println(e);
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }
    
    
    
    
    //Method to create keystore or load one
    public void generateKeyStore(User user){
        try{
            File folder = new File("Security");
            if(!folder.exists()){
                folder.mkdirs();
            }

            char[] password = user.password.toCharArray();
            KeyStore ks = KeyStore.getInstance("PKCS12");//PKCS12 is the modern KeyStore type
            ks.load(null, password);//Create empty keystore 
            //Create an AES key
            KeyGenerator newKey = KeyGenerator.getInstance("AES");
            newKey.init(KEY_SIZE);
            SecretKey actualNewKey = newKey.generateKey();

            KeyStore.SecretKeyEntry keyToStore = new KeyStore.SecretKeyEntry(actualNewKey);
            KeyStore.ProtectionParameter ksPassword = new KeyStore.PasswordProtection(password);

            try{//Create keystore file
                ks.setEntry(user.username, keyToStore, ksPassword);
                String theFileName = "Security/" + user.mainFileNum + ".p12";
                FileOutputStream out = new FileOutputStream(theFileName);
                ks.store(out, password);
            }catch(Exception e){
                System.out.println(e);
            }
        }catch(Exception e){
            System.out.println(e);
        }
        
    }
    

}
