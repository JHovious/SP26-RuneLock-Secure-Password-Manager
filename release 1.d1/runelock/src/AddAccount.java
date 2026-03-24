


import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author ckurd
 */
public class AddAccount {
    //Maybe pass old objects for window control
    
    public void showWindow(){
        Stage stage = new Stage();
        Label title = new Label("Create An Account");
        title.setStyle("-fx-font-size: 100px; -fx-text-fill: #0076a3;");
        
        Label websiteLabel = new Label("Enter website URL:");
        //Need format verification for allowable URL
        TextField websiteText = new TextField();
        websiteText.setPromptText("Enter URL");
        
        Label usernameLabel = new Label("Enter your username:");
        //Need format verification for allowable input
        TextField usernameText = new TextField();
        usernameText.setPromptText("MAKE SURE YOU ARE PUTTING THE CORRECT USERNAME");
        
        Label passwordLabel = new Label("Enter your password:");
        //Need format verification for allowable input
        TextField passwordText = new TextField();
        passwordText.setPromptText("MAKE SURE YOU ARE PUTTING THE CORRECT PASSWORD");
        
        Label tagLabel = new Label("Would you like to associate this account with a tag?");
        Label tagSelectionLabel = new Label();
        //Need format verification for allowable input
        RadioButton yesOption = new RadioButton("Yes");
        RadioButton noOption = new RadioButton("No");
        
        ToggleGroup group = new ToggleGroup();
        yesOption.setToggleGroup(group);
        noOption.setToggleGroup(group);
        
        
        Label tagName = new Label("Enter the tag name");
        TextField tagText = new TextField();
        tagText.setPromptText("Enter the tag name");
        VBox tagBox = new VBox(tagName, tagText);
        tagBox.setStyle("-fx-padding: 20px;");
        tagBox.setVisible(false);
        
        //Toggle Group event listener
        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                RadioButton selected = (RadioButton) newValue;
                tagSelectionLabel.setText("You selected: " + selected.getText());
                if ("Yes".equals(selected.getText())){
                    tagBox.setVisible(true);
                }else{
                    tagBox.setVisible(false);
                }
                
            }
        });
        
        //Submit button
        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String url = websiteText.getText();
            String username = usernameText.getText();
            String password = passwordText.getText();
            String tag = tagText.getText();

            //do something with the variables above
            stage.close();
        });
        
        //Main box
        VBox vbox = new VBox(title, websiteLabel, websiteText, usernameLabel, usernameText, passwordLabel, passwordText, tagLabel, yesOption, noOption, tagSelectionLabel);
        //Submit box
        VBox submitBox = new VBox(submitButton);
        submitBox.setStyle("-fx-padding: 20px;");
        
        //Main layout
        BorderPane mainPane = new BorderPane();
        mainPane.setTop(vbox);
        mainPane.setCenter(tagBox);
        mainPane.setBottom(submitBox);
        Scene scene = new Scene(mainPane, 1000, 1000); //Window
        
        stage.setScene(scene);
        stage.setTitle("RuneLock");
        stage.show();
    }
}
