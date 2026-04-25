import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;

/**
 *
 * @author ckurd
 */
public class SEProject extends Application{
    
    private LanSyncService lanSync;
    
    @Override
    
    public void start(Stage stage){
        JSONControl controller = new JSONControl();
        mainWindow main = new mainWindow();
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
            if (controller.verifyLogin(username, password)){
                stage.close();
                
                // Start LAN discovery for logged-in user.
                lanSync = new LanSyncService(controller, 50506);
                lanSync.start();
                
                if (controller.getUser().isSub()) {
                    // If subuser, only load their accounts
                    controller.loadAccountFiles();
                    main.showMenuForSubUser(controller, lanSync);
                } else {
                    // Load main user accounts and subuser accounts
                    controller.loadAccountFiles();
                    main.showMenu(controller, lanSync);
                }
            } else {
                stage.close();
            }
        });
        
         
        Button createButton = new Button("Create Account");
        CreateUserWindow createWindow = new CreateUserWindow();
        
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
