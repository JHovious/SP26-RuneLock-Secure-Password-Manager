/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */

import java.io.File;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

/**
 *
 * @author ckurd
 */
public class SEProject extends Application{

    @Override
    
    public void start(Stage stage){
        
        JSONControl controller = new JSONControl();
        mainWindow main = new mainWindow();
        CreateUserWindow createWindow = new CreateUserWindow();
        
        // aFile = new File("security.p12");
        //if (!aFile.exists()){
            //createWindow.show(controller);
        //}
            
        Label usernameLabel = new Label("Username:");
        //usernameLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #0076a3;");
        TextField usernameText = new TextField();
        usernameText.setPromptText("Username");

        Label passwordLabel = new Label("Password:");
        //passwordLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #0076a3;");
        TextField passwordText = new TextField();
        passwordText.setPromptText("Password");

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {

            String username = usernameText.getText();
            String password = passwordText.getText();

            System.out.println("Username: " + username);
            System.out.println("Password: " + password);
            int check = controller.verifyLogin(username, password);
            if (check == 1){
                stage.close();
                controller.loadAccountFiles();
                main.showMenu(controller);
            }else if (check == 2){
                createWindow.show(controller);
            }else{
                System.out.println("Login Failed");
                stage.close();
            }

        });

        // 
        Button createButton = new Button("Create Account");

        createButton.setOnAction(e -> {
            createWindow.show(controller);
        });




        VBox vbox = new VBox(usernameLabel, usernameText, passwordLabel, passwordText, submitButton, createButton);
        Scene scene = new Scene(vbox, 300, 200); //Window

        stage.setScene(scene);
        stage.setTitle("RuneLock");
        stage.show();
        
    }
    

    public static void main(String[] args) {
        launch(args);
    }
    
}
