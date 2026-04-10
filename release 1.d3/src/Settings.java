/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * This class handles the UI for the Settings window.
 * Logic is managed through JSONControl.java
 * @author jahov
 */
public class Settings {

    private Stage stage;

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
        syncButton.setOnAction(e -> System.out.println("Vaults synced successfully"));

        // Main user management
        Button changeUsername = new Button("Change Username");
        changeUsername.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(controller.getUser().getUsername());
            dialog.setTitle("Change Username");
            dialog.setHeaderText("Update your username");
            dialog.setContentText("New username:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newName -> {
                controller.getUser().username = newName;
                controller.storeMainAccount(controller.getUser().getUsername(), controller.getUser().getPassword());
                System.out.println("Username updated.");
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
                controller.getUser().password = newPass;
                controller.storeMainAccount(controller.getUser().getUsername(), controller.getUser().getPassword());
                System.out.println("Password updated.");
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
                    addSubUser,
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
