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

import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * This class handles all UI elements seen in the main window after login.
 * Logic is handled by JSONControl.java
 * @author ckurd
 */
public class mainWindow {

    private TableView<Account> table;
    private ObservableList<Account> accountList;

    public void showMenu(JSONControl controller) {

        // Load icons
        Image settingsImage;
        Image plusImage;

        try {
            settingsImage = new Image(getClass().getResourceAsStream("/images/setting.png"));
            plusImage = new Image(getClass().getResourceAsStream("/images/plus.png"));
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
            return;
        }

        // Settings button
        ImageView settingsView = new ImageView(settingsImage);
        settingsView.setFitWidth(24);
        settingsView.setFitHeight(24);
        settingsView.setPreserveRatio(true);

        Button settingsButton = new Button();
        settingsButton.setGraphic(settingsView);
        settingsButton.setOnAction(e -> {
            Settings settingsWindow = new Settings();
            settingsWindow.showSettings(controller);
        });

        // Add Account button
        ImageView addView = new ImageView(plusImage);
        addView.setFitWidth(30);
        addView.setFitHeight(30);
        addView.setPreserveRatio(true);

        Button addPasswordButton = new Button("Add Account");
        addPasswordButton.setGraphic(addView);
        addPasswordButton.setOnAction(e -> {
            AddAccount addWindow = new AddAccount();
            addWindow.showWindow(controller);
            loadAccounts(controller);
        });

        // Add SubUser button
        ImageView subView = new ImageView(plusImage);
        subView.setFitWidth(30);
        subView.setFitHeight(30);
        subView.setPreserveRatio(true);

        Button addSubButton = new Button("Add SubUser");
        addSubButton.setGraphic(subView);
        addSubButton.setOnAction(e -> {
            AddSub addSubWindow = new AddSub();
            addSubWindow.showWindow(controller);
        });

        // Search bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(300);

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> {
            String search = searchField.getText().trim();
            System.out.println("Searching for: " + search);
            // Filtering logic to be added later
        });

        Button filterButton = new Button("Filter");

        HBox topBox = new HBox(10, filterButton, searchField, searchButton);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(20, 0, 0, 0));

        // JSON database view
        table = new TableView<>();

        TableColumn<Account, String> urlCol = new TableColumn<>("URL");
        urlCol.setCellValueFactory(new PropertyValueFactory<>("url"));

        TableColumn<Account, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Account, String> tagCol = new TableColumn<>("Tags");
        tagCol.setCellValueFactory(new PropertyValueFactory<>("tags"));

        TableColumn<Account, String> createdCol = new TableColumn<>("Created");
        createdCol.setCellValueFactory(new PropertyValueFactory<>("created"));

        TableColumn<Account, String> usedCol = new TableColumn<>("Last Used");
        usedCol.setCellValueFactory(new PropertyValueFactory<>("lastUsed"));

        TableColumn<Account, Integer> fileCol = new TableColumn<>("File #");
        fileCol.setCellValueFactory(new PropertyValueFactory<>("fileNumber"));

        table.getColumns().addAll(urlCol, userCol, tagCol, createdCol, usedCol, fileCol);

        loadAccounts(controller);

        // CRUD buttons
        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> {
            Account selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            EditAccountWindow editWindow = new EditAccountWindow();
            editWindow.showWindow(controller, selected);
            loadAccounts(controller);
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> {
            Account selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            controller.deleteAccount(selected);
            loadAccounts(controller);
        });

        // Not currently functional. Changes can only be seen after signing out
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadAccounts(controller));

        HBox crudButtons = new HBox(10, addPasswordButton, editButton, deleteButton, refreshButton, addSubButton);
        crudButtons.setAlignment(Pos.CENTER);
        crudButtons.setPadding(new Insets(10));

        VBox centerBox = new VBox(10, table, crudButtons);
        centerBox.setPadding(new Insets(20));

        // Bottom bar
        HBox bottomBox = new HBox(15, settingsButton);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setPadding(new Insets(20));

        // Main layout
        BorderPane mainPane = new BorderPane();
        mainPane.setTop(topBox);
        mainPane.setCenter(centerBox);
        mainPane.setBottom(bottomBox);

        Stage stage = new Stage();
        Scene scene = new Scene(mainPane, 800, 600);
        stage.setScene(scene);
        stage.setTitle("RuneLock");
        stage.show();
    }

    // Pulls data from JSON files into database view
    private void loadAccounts(JSONControl controller) {
        accountList = FXCollections.observableArrayList(controller.getUser().getAccounts());
        table.setItems(accountList);
    }
}
