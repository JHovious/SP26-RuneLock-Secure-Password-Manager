/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 *
 * @author jhov
 */
public class Settings {
    
    private Stage stage;
    
    public void showSettings() {
        
        stage = new Stage();
        
        BorderPane mainPane = new BorderPane();
        
        //Window title style
        Label title = new Label("Settings");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        
        //Select theme
        Label themeLabel = new Label("Theme:");
        ComboBox<String> themeBox = new ComboBox<>();
        themeBox.getItems().addAll("Light", "Dark", "System Default");
        themeBox.setValue("Light");
        
        HBox themeRow = new HBox(10, themeLabel, themeBox);
        themeRow.setAlignment(Pos.CENTER_LEFT);
        
        //Notifications toggle
        CheckBox notifications = new CheckBox("Enable Notifications");
        notifications.setSelected(true);
        
        // Manual sync button
        Button syncButton = new Button("Sync vaults");
        syncButton.setStyle("-fx-padding: 8px 20px; -fx-font-size: 14px;");
        syncButton.setOnAction(e -> {
            System.out.println("Vaults synced successfully");
        });
        
        //Save button
        Button saveButton = new Button("Save Settings");
        saveButton.setStyle("-fx-padding: 8px 20px; -fx-font-size: 14px;");
        saveButton.setOnAction(e -> {
           System.out.println("Settings have been saved"); 
        });
        
        //Sign out button
        Button signOut = new Button("Sign Out");
        signOut.setStyle("-fx-padding: 8px 20px; -fx-font-size: 14px;");
        signOut.setOnAction(e -> {
            //Close settings window
            stage.close();
            
            //Copy list of windows to be closed
            List<Window> windows = new ArrayList<>(Window.getWindows());
            
            //Iterate through open windows, closing them if they're a "stage"
            for (Window w : windows) {
                if (w instanceof Stage && w != stage) {
                    ((Stage)w).close();
                }
            }
            
            //Return to login page
            SEProject login = new SEProject();
            Stage loginStage = new Stage();
            try {
                login.start(loginStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
        });
        
        //Settings layout
        VBox settingsBox = new VBox(20, title, themeRow, notifications, syncButton, saveButton, signOut);
        settingsBox.setAlignment(Pos.TOP_CENTER);
        settingsBox.setPadding(new Insets(40, 40, 40, 40));
        
        mainPane.setCenter(settingsBox);
        
        //Build window 
        
        Scene scene = new Scene(mainPane, 400, 400); //Window
        stage.setScene(scene);
        stage.setTitle("Settings");
        stage.show();
    }
    
}
