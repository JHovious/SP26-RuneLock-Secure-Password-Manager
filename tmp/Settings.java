import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * This class handles the UI for the Settings window.
 * Logic is managed through JSONControl.java
 * @author Justin Hovious
 */
public class Settings {
    

    private Stage stage;
    private LanSyncService lanSync;
    
    public Settings(LanSyncService lanSync) {
        System.out.println("Settings constructor received lanSync = " + lanSync);
        this.lanSync = lanSync;
    }
    
    private void showDeviceManager(JSONControl controller) {
        Stage stage = new Stage();
        stage.setTitle("Authorized Devices");

        String localId = controller.getLocalDeviceId();

        Label localLabel = new Label("This Device ID:");
        TextField localField = new TextField(localId);
        localField.setEditable(false);

        ListView<String> list = new ListView<>();
        list.getItems().addAll(controller.getAuthorizedDevices());

        Button addBtn = new Button("Add Device");
        addBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Device");
            dialog.setHeaderText("Enter the device ID to authorize:");
            dialog.setContentText("Device ID:");
            dialog.showAndWait().ifPresent(id -> {
                controller.authorizeDevice(id.trim());
                list.getItems().setAll(controller.getAuthorizedDevices());
            });
        });

        Button removeBtn = new Button("Remove Selected");
        removeBtn.setOnAction(e -> {
            String selected = list.getSelectionModel().getSelectedItem();
            if (selected != null) {
                controller.removeAuthorizedDevice(selected);
                list.getItems().setAll(controller.getAuthorizedDevices());
            }
        });

        VBox layout = new VBox(10,
            localLabel, localField,
            new Label("Authorized Devices:"),
            list,
            addBtn, removeBtn
        );
        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 400, 500));
        stage.show();
    }


    public void showSettings(JSONControl controller) {
        
        stage = new Stage();
        boolean isSub = controller.getUser().isSub();
        BorderPane mainPane = new BorderPane();
        

        // Title
        Label title = new Label("Settings");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        // Theme selector
        Label themeLabel = new Label("Theme:");
        ComboBox<String> themeBox = new ComboBox<>();
        themeBox.getItems().addAll("Light", "Dark", "System Default");
        themeBox.setValue("Light");

        HBox themeRow = new HBox(10, themeLabel, themeBox);
        themeRow.setAlignment(Pos.CENTER_LEFT);

        // Sync button
        Button syncButton = new Button("Sync vaults");
        syncButton.setStyle("-fx-padding: 8px 20px; -fx-font-size: 14px;");
        syncButton.setOnAction(e -> {
            System.out.println("Manual sync requested.");
        });
        
        // Manage Devices Buttton
        Button manageDevices = new Button("Manage Devices");
        manageDevices.setOnAction(e -> showDeviceManager(controller));

        // Main user management
        Button changeUsername = new Button("Change Username");
        changeUsername.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(controller.getUser().getUsername());
            dialog.setTitle("Change Username");
            dialog.setHeaderText("Update your username");
            dialog.setContentText("New username:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newName -> {

                // 1. Save old credentials for keystore update
                controller.getUser().setPastUsername(controller.getUser().getUsername());
                controller.getUser().setPastPassword(controller.getUser().getPassword());

                // 2. Apply new username
                controller.getUser().username = newName;

                // 3. Update keystore (0 = username changed)
                controller.handleUpdates(0);

                System.out.println("Username updated (encrypted + keystore rotated).");
            });
        });


        Button changePassword = new Button("Change Password");
        changePassword.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Change Password");
            dialog.setHeaderText("Update your password");
            dialog.setContentText("New password:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newPass -> {

                // 1. Save old credentials for keystore update
                controller.getUser().setPastUsername(controller.getUser().getUsername());
                controller.getUser().setPastPassword(controller.getUser().getPassword());

                // 2. Apply new password
                controller.getUser().password = newPass;

                // 3. Update keystore (1 = password changed)
                controller.handleUpdates(1);

                System.out.println("Password updated (encrypted + keystore rotated).");
            });
        });
        
        


        // SubUser Management
        Button addSubUser = new Button("Add SubUser");
        addSubUser.setOnAction(e -> {
            AddSub addSubWindow = new AddSub();
            addSubWindow.showWindow(controller);
        });

        Button updateSubUser = new Button("Update SubUser");
        updateSubUser.setOnAction(e -> showSubUserSelectionWindow(controller, "update"));

        Button deleteSubUser = new Button("Delete SubUser");
        deleteSubUser.setOnAction(e -> showSubUserSelectionWindow(controller, "delete"));

        // Sign out button
        Button signOut = new Button("Sign Out");
        signOut.setStyle("-fx-padding: 8px 20px; -fx-font-size: 14px;");
        signOut.setOnAction(e -> {
            if (lanSync != null) {
                lanSync.stop();
            }
            stage.close();

            List<Window> windows = new ArrayList<>(Window.getWindows());
            for (Window w : windows) {
                if (w instanceof Stage && w != stage) {
                    ((Stage) w).close();
                }
            }

            SEProject login = new SEProject();
            Stage loginStage = new Stage();
            try {
                login.start(loginStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        

        // Layout
        VBox settingsBox = new VBox(
                20,
                title,
                themeRow,
                syncButton,
                signOut
        );
        
        if (!isSub) {
            settingsBox.getChildren().addAll(
                    changeUsername,
                    changePassword,
                    manageDevices,
                    updateSubUser,
                    deleteSubUser
            );
        }

        settingsBox.setAlignment(Pos.TOP_CENTER);
        settingsBox.setPadding(new Insets(40, 40, 40, 40));

        mainPane.setCenter(settingsBox);

        Scene scene = new Scene(mainPane, 400, 600);
        stage.setScene(scene);
        stage.setTitle("Settings");
        stage.show();
    }

    // SubUser selection window 
    private void showSubUserSelectionWindow(JSONControl controller, String action) {
        Stage stage = new Stage();
        stage.setTitle("Select SubUser");

        ListView<String> list = new ListView<>();
        for (User.SubUserMeta su : controller.getUser().subAccounts) {
            list.getItems().add(su.username);
        }

        Button confirm = new Button("Confirm");
        confirm.setOnAction(e -> {
            String selected = list.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            User.SubUserMeta target = controller.getUser().subAccounts
                    .stream()
                    .filter(su -> su.username.equals(selected))
                    .findFirst()
                    .orElse(null);

            if (target == null) return;

            if (action.equals("delete")) {
                controller.deleteSubUser(target);
                System.out.println("SubUser deleted.");
            }

            if (action.equals("update")) {
                showSubUserUpdateWindow(controller, target);
            }

            stage.close();
        });

        VBox layout = new VBox(10, list, confirm);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(layout, 300, 300));
        stage.show();
    }

    // SubUser update window
    private void showSubUserUpdateWindow(JSONControl controller, User.SubUserMeta target) {
        Stage stage = new Stage();
        stage.setTitle("Update SubUser");

        TextInputDialog userDialog = new TextInputDialog(target.username);
        userDialog.setTitle("Update SubUser");
        userDialog.setHeaderText("Update Username");
        userDialog.setContentText("New username:");
        Optional<String> newUser = userDialog.showAndWait();

        TextInputDialog passDialog = new TextInputDialog();
        passDialog.setTitle("Update SubUser");
        passDialog.setHeaderText("Update Password");
        passDialog.setContentText("New password:");
        Optional<String> newPass = passDialog.showAndWait();

        if (newUser.isPresent() && newPass.isPresent()) {
            controller.updateSubUser(target, newUser.get(), newPass.get());
            System.out.println("SubUser updated.");
        }

        stage.close();
    }
}
