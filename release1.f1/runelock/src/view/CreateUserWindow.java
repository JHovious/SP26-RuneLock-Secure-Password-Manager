package view;


import controller.JSONControl;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Modality;


/**
 * Uses JSON controller to create a new user and generate a new JSON file in 
 * MainAccounts/
 * @author Justin Hovious
 */
public class CreateUserWindow {

    public void show(JSONControl controller) {
        Stage stage = new Stage();
        
        // This section holds all buttons and labels
        Label title = new Label("Create New Account");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        userField.setPromptText("Enter username");

        Label passLabel = new Label("Password:");
        TextField passField = new TextField();
        passField.setPromptText("Enter password");

        Button createButton = new Button("Create Account");
        createButton.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                System.out.println("Username or password cannot be empty.");
                return;
            }

            controller.createMainAccount(username, password);
            System.out.println("Account created successfully.");

            stage.close(); // Return to login screen
        });

        VBox layout = new VBox(15, title, userLabel, userField, passLabel, passField, createButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 350, 300);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}

