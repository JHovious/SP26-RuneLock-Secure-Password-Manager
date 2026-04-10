import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import java.util.ArrayList;
import javafx.stage.Modality;

/**
 *
 * @author ckurd
 */
public class AddSub {
    
    public void showWindow(JSONControl aController){
        JSONControl controller = aController;
        Stage stage = new Stage();
        Label title = new Label("Create A Sub Account");
        title.setStyle("-fx-font-size: 50px; -fx-text-fill: #0076a3;");
        
        Label nameLabel = new Label("Enter the user's name for the sub account:");
        TextField nameText = new TextField();
        nameText.setPromptText("Name of Sub User");
        
        Label usernameLabel = new Label("Enter their username:");
        //Need format verification for allowable input
        TextField usernameText = new TextField();
        usernameText.setPromptText("MAKE SURE YOU ARE PUTTING THE CORRECT USERNAME");
        
        Label passwordLabel = new Label("Enter their password:");
        //Need format verification for allowable input
        TextField passwordText = new TextField();
        passwordText.setPromptText("MAKE SURE YOU ARE PUTTING THE CORRECT PASSWORD");
        
        

        
        //Submit button
        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String name = nameText.getText();
            String username = usernameText.getText();
            String password = passwordText.getText();

            controller.storeSubAccount(name, username, password);
            stage.close();
        });
        
        //Main box
        VBox vbox = new VBox(title, nameLabel, nameText, usernameLabel, usernameText, passwordLabel, passwordText);
        //Submit box
        VBox submitBox = new VBox(submitButton);
        submitBox.setStyle("-fx-padding: 20px;");
        
        //Main layout
        BorderPane mainPane = new BorderPane();
        mainPane.setTop(vbox);
        mainPane.setBottom(submitBox);
        Scene scene = new Scene(mainPane, 600, 600); //Window
        
        stage.setScene(scene);
        stage.setTitle("RuneLock");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}
