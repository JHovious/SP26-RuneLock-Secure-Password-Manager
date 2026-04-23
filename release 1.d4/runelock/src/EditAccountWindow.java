import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;


/**
 * This class controls the view and logic for the "Edit Account" window 
 * @author jahov
 */
public class EditAccountWindow {

    public void showWindow(JSONControl controller, Account account) {

        Stage stage = new Stage();

        Label title = new Label("Edit Account");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label urlLabel = new Label("Website URL:");
        TextField urlField = new TextField(account.getUrl());

        Label userLabel = new Label("Username:");
        TextField userField = new TextField(account.getUsername());

        Label passLabel = new Label("Password:");
        TextField passField = new TextField(account.getPassword());

        Label tagLabel = new Label("Tags (comma separated):");
        TextField tagField = new TextField(
          String.join(",", account.getTags().toArray(new String[0]))      
        );

        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> {

            account.setUrl(urlField.getText().trim());
            account.setUsername(userField.getText().trim());
            account.setPassword(passField.getText().trim());

            // Convert comma-separated tags into ArrayList<String>
            String[] tagArray = tagField.getText().trim().split(",");
            account.setTags(new java.util.ArrayList<>(java.util.Arrays.asList(tagArray)));

            controller.updateAccount(account);

            System.out.println("Account updated successfully.");
            stage.close();
        });

        VBox layout = new VBox(12,
                title,
                urlLabel, urlField,
                userLabel, userField,
                passLabel, passField,
                tagLabel, tagField,
                saveButton
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 350, 450);
        stage.setScene(scene);
        stage.setTitle("Edit Account");
        stage.show();
    }
}
