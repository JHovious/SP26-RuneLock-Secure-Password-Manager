
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author ckurd
 */
public class Security {
    
    public Boolean verifyLogin(String username, String password){
        //Code for verifying login
        try{
            ObjectMapper myMapper = new ObjectMapper();
            InputStream in = getClass().getResourceAsStream("/mainAccounts/0.json");
            User user = myMapper.readValue(in, User.class);
            if (user.username.equals(username)){
                if (user.password.equals(password)){
                    return true;
                }else{
                        return false;
                }
            }else{
                    return false;
                }
        }catch (Exception e){
            System.out.println("There was an error loading mainFile.json for verification");
        }
    
        return true;
    }
}
