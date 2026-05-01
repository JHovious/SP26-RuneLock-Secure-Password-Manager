package controller;

import view.CreateUserWindow;
import view.mainWindow;
import controller.JSONControl;
import model.LanSyncService;
import javafx.application.Application;
import javafx.application.Platform;
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
public class App extends Application {

    private LanSyncService lanSync;

    @Override
    public void start(Stage stage) {
        JSONControl controller = new JSONControl();
        mainWindow main = new mainWindow();

        Label usernameLabel = new Label("Username:");
        TextField usernameText = new TextField();
        usernameText.setPromptText("Username");

        Label passwordLabel = new Label("Password:");
        TextField passwordText = new TextField();
        passwordText.setPromptText("Password");

        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #888888;");

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String username = usernameText.getText().trim();
            String password = passwordText.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please enter a username and password.");
                return;
            }

            // Check if this is a fresh device with no local account files
            java.io.File mainAccountsFolder = new java.io.File("MainAccounts");
            boolean isFreshDevice = !mainAccountsFolder.exists()
                    || mainAccountsFolder.listFiles() == null
                    || mainAccountsFolder.listFiles((dir, name) -> name.endsWith(".enc")).length == 0;

            if (isFreshDevice) {
                // Attempt to pull login files from another device on the network
                statusLabel.setText("No local account found. Looking for account on network...");
                submitButton.setDisable(true);

                // Run bootstrap on a background thread so the UI doesn't freeze
                Thread bootstrapThread = new Thread(() -> {
                    boolean received = LanSyncService.requestBootstrap(username);

                    Platform.runLater(() -> {
                        submitButton.setDisable(false);
                        if (received) {
                            statusLabel.setText("Account files received. Logging in...");
                            attemptLogin(username, password, controller, main, stage, statusLabel);
                        } else {
                            statusLabel.setText("No peer found on network. Check that your other device is on and logged in.");
                        }
                    });
                }, "RuneLock-Bootstrap");

                bootstrapThread.setDaemon(true);
                bootstrapThread.start();

            } else {
                // Normal login — files already exist locally
                attemptLogin(username, password, controller, main, stage, statusLabel);
            }
        });

        Button createButton = new Button("Create Account");
        CreateUserWindow createWindow = new CreateUserWindow();
        createButton.setOnAction(e -> createWindow.show(controller));

        VBox vbox = new VBox(10,
                usernameLabel, usernameText,
                passwordLabel, passwordText,
                statusLabel,
                submitButton, createButton);

        vbox.setStyle("-fx-padding: 20px;");

        Scene scene = new Scene(vbox, 300, 250);
        stage.setScene(scene);
        stage.setTitle("RuneLock");
        stage.show();
    }

    /**
     * Attempts login with the given credentials and opens the main window on success.
     * Extracted into its own method so it can be called both from normal login
     * and after a successful bootstrap.
     */
    private void attemptLogin(String username, String password, JSONControl controller,
                               mainWindow main, Stage stage, Label statusLabel) {
        if (controller.verifyLogin(username, password)) {
            stage.close();

            lanSync = new LanSyncService(controller, 50506);
            lanSync.start();

            if (controller.getUser().isSub()) {
                controller.loadAccountFiles();
                main.showMenuForSubUser(controller, lanSync);
            } else {
                controller.loadAccountFiles();
                main.showMenu(controller, lanSync);
            }
        } else {
            statusLabel.setText("Invalid username or password.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
