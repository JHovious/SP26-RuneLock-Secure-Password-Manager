

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;


/**
 *
 * @author ckurd, jhov
 */
public class mainWindow {
    
    public void showMenu(JSONControl aController){
         JSONControl controller = aController;
         
        //My code
        Image settingsImage;
        Image plusImage;
        Image dummyImage;
        AddAccount addAccountObject = new AddAccount();
        Settings settingsWindow = new Settings();
         try{//Read in Settings image
             settingsImage = new Image(getClass().getResourceAsStream("/images/setting.png"));
         } catch(Exception e){
             System.err.println("Error loading settings image: " + e.getMessage());
             return;
         }
         try{//Read in plus image
             plusImage = new Image(getClass().getResourceAsStream("/images/plus.png"));
         }catch(Exception e){
             System.err.println("Error loading plus image: " + e.getMessage());
             return;
         }
         try{//Read in dummy database image
             dummyImage = new Image(getClass().getResourceAsStream("/images/dummy_db.png"));
         }catch(Exception e){
             System.err.println("Error loading db image: " + e.getMessage());
             return;
         }
        
         
         //Create imageView for Settings button and create button
         ImageView settingsView = new ImageView(settingsImage);
         settingsView.setFitWidth(24);
         settingsView.setFitHeight(24);
         settingsView.setPreserveRatio(true);
         Button settingsButton = new Button();
         settingsButton.setGraphic(settingsView);
         settingsButton.setOnAction(e -> {
            System.out.println("Opening settings window");
            settingsWindow.showSettings();
        });
         
         //Create imageView for add password button and create button
         ImageView addView = new ImageView(plusImage);
         addView.setFitWidth(30);
         addView.setFitHeight(30);
         addView.setPreserveRatio(true);
         Button addPasswordButton = new Button("Add An Account");
         addPasswordButton.setGraphic(addView);
         //Event listener for add password window
         addPasswordButton.setOnAction(e -> {
            System.out.println("Opening add password window");
            addAccountObject.showWindow();
        });
         
         //Create imageView for dummy database
         ImageView dummyView = new ImageView(dummyImage);
         dummyView.setFitWidth(800);
         dummyView.setFitHeight(800);
         dummyView.setPreserveRatio(true);
         
         //Aligning dummy database to center of scene
         VBox centerBox = new VBox(dummyView);
         centerBox.setAlignment(Pos.TOP_CENTER);
         centerBox.setPadding(new Insets(20, 0, 0, 0));
         
         //Create filter button
         Button filterButton = new Button("Filter");
         filterButton.setOnAction(e -> {
             System.out.println("Opening filter options");
             // Open filter options
         });
         
         //Create search bar
         TextField searchField = new TextField();
         searchField.setPromptText("Search...");
         searchField.setPrefWidth(300);
         
         //Create search button and gets input from search bar
         Button searchButton = new Button("Search");
         searchButton.setOnAction(e -> {
             String search = searchField.getText();
             System.out.println("Searching database for: " + search);
         });
         
         
         //Make box for filter, search bar, and search button
         HBox topBox = new HBox(10, filterButton, searchField, searchButton);
         topBox.setAlignment(Pos.CENTER);
         topBox.setPadding(new Insets(20, 0, 0, 0));
        
         
         //Make box for Settings and add password
         HBox bottomBox = new HBox(15, settingsButton, addPasswordButton);
         bottomBox.setStyle("-fx-padding: 20px;");
         
         //BorderPane for controlling overall layout
         BorderPane mainPane = new BorderPane();
         //For password view : mainPane.setCenter();
         mainPane.setTop(topBox);
         mainPane.setCenter(centerBox);
         mainPane.setBottom(bottomBox);
         
         
        //Build window 
        Stage stage = new Stage();
        Scene scene = new Scene(mainPane, 600, 600); //Window
        stage.setScene(scene);
        stage.setTitle("RuneLock");
        stage.show();
        
    }
}
