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
import javafx.stage.Modality;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

/**
 * This class handles all UI elements seen in the main window after login.
 * Logic is handled by JSONControl.java
 * @author ckurd
 */
public class mainWindow {
    
    private LanSyncService lanSync;
    private TableView<Account> table;
    private ObservableList<Account> accountList;

    public void showMenu(JSONControl controller, LanSyncService lanSync) {
        this.lanSync = lanSync;

        // Settings Button
        Button settingsButton = new Button();
        settingsButton.setGraphic(IconUtil.loadIcon("/images/setting.png", 24));
        settingsButton.setOnAction(e -> {
            Settings settingsWindow = new Settings(lanSync);
            settingsWindow.showSettings(controller);
        });

        // Add Account button
        Button addPasswordButton = new Button("Add Account");
        addPasswordButton.setGraphic(IconUtil.loadIcon("/images/plus.png", 30));
        addPasswordButton.setOnAction(e -> {
            AddAccount addWindow = new AddAccount();
            addWindow.showWindow(controller);
            loadAccounts(controller);
        });

        // Add SubUser button
        Button addSubButton = new Button("Add SubUser");
        addSubButton.setGraphic(IconUtil.loadIcon("/images/plus.png", 30));
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
            String search = searchField.getText().trim().toLowerCase();
            
            ObservableList<Account> filtered = accountList.filtered(acc ->
            acc.getUrl().toLowerCase().contains(search) ||
            acc.getUsername().toLowerCase().contains(search) ||
            acc.getTags().toString().toLowerCase().contains(search)
            );

            table.setItems(filtered);
            
        });
        
        Button clearSearch = new Button("Clear");
        clearSearch.setOnAction(e -> {
            searchField.clear();
            table.setItems(accountList);
        });

        Button filterButton = new Button("Filter");
        filterButton.setOnAction(e -> {
            Stage filterStage = new Stage();
            filterStage.initModality(Modality.APPLICATION_MODAL);

            ComboBox<String> tagBox = new ComboBox<>();
            tagBox.getItems().add("All Tags");

            // Collect unique tags
            controller.getUser().getAccounts().forEach(acc -> {
                acc.getTags().forEach(tag -> {
                    if (!tagBox.getItems().contains(tag))
                        tagBox.getItems().add(tag);
                });
            });

            tagBox.setValue("All Tags");

            ComboBox<String> userBox = new ComboBox<>();
            userBox.getItems().addAll("All Users", "Main User", "SubUsers");
            userBox.setValue("All Users");

            Button apply = new Button("Apply");

            apply.setOnAction(ev -> {
                String tag = tagBox.getValue();
                String userType = userBox.getValue();

                ObservableList<Account> filtered = accountList.filtered(acc -> {
                    boolean tagMatch = tag.equals("All Tags") || acc.getTags().contains(tag);

                    boolean userMatch = true;
                    String owner = acc.getOwnerUid();
                    if ("Main User".equals(userType)) {
                        userMatch = owner != null && owner.equals(controller.getUser().getuid());
                    } else if ("SubUsers".equals(userType)) {
                        userMatch = owner != null && !owner.equals(controller.getUser().getuid());
                    }

                    return tagMatch && userMatch;
                });

                table.setItems(filtered);
                filterStage.close();
            });

            VBox box = new VBox(10, new Label("Filter by Tag:"), tagBox,
                                     new Label("Filter by User:"), userBox,
                                     apply);
            box.setPadding(new Insets(20));

            filterStage.setScene(new Scene(box, 300, 250));
            filterStage.showAndWait();
        });

        HBox topBox = new HBox(10, filterButton, searchField, searchButton, clearSearch);
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

        table.getColumns().addAll(urlCol, userCol, tagCol, createdCol);
        
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Account selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) return;

                Stage popup = new Stage();
                popup.initModality(Modality.APPLICATION_MODAL);

                Label url = new Label("URL: " + selected.getUrl());
                Label user = new Label("Username: " + selected.getUsername());

                PasswordField hidden = new PasswordField();
                hidden.setText(selected.getPassword());

                TextField revealed = new TextField(selected.getPassword());
                revealed.setVisible(false);

                CheckBox show = new CheckBox("Show password");
                show.setOnAction(e -> {
                    boolean s = show.isSelected();
                    hidden.setVisible(!s);
                    revealed.setVisible(s);
                });

                VBox box = new VBox(10, url, user, hidden, revealed, show);
                box.setPadding(new Insets(20));

                popup.setScene(new Scene(box, 300, 200));
                popup.showAndWait();
            }
        });

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
        controller.loadAccountFiles();
        
        accountList = FXCollections.observableArrayList(controller.getUser().getAccounts());
        table.setItems(accountList);
        table.refresh();
    }
    
    public void showMenuForSubUser(JSONControl controller, LanSyncService lanSync) {
        // Restricted version of main menu for subusers
        table = new TableView<>();
        
        
        TableColumn<Account, String> urlCol = new TableColumn<>("URL");
        urlCol.setCellValueFactory(new PropertyValueFactory<>("url"));

        TableColumn<Account, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Account, String> tagCol = new TableColumn<>("Tags");
        tagCol.setCellValueFactory(new PropertyValueFactory<>("tags"));

        TableColumn<Account, String> createdCol = new TableColumn<>("Created");
        createdCol.setCellValueFactory(new PropertyValueFactory<>("created"));

        table.getColumns().addAll(urlCol, userCol, tagCol, createdCol);
        
        table.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2) {
            Account selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);

            Label url = new Label("URL: " + selected.getUrl());
            Label user = new Label("Username: " + selected.getUsername());

            PasswordField hidden = new PasswordField();
            hidden.setText(selected.getPassword());

            TextField revealed = new TextField(selected.getPassword());
            revealed.setVisible(false);

            CheckBox show = new CheckBox("Show password");
            show.setOnAction(e -> {
                boolean s = show.isSelected();
                hidden.setVisible(!s);
                revealed.setVisible(s);
            });

            VBox box = new VBox(10, url, user, hidden, revealed, show);
            box.setPadding(new Insets(20));

            popup.setScene(new Scene(box, 300, 200));
            popup.showAndWait();
        }
    });

        loadAccounts(controller);
        
        // Search Bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(300);

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> {
            String search = searchField.getText().trim().toLowerCase();
            ObservableList<Account> filtered = accountList.filtered(acc ->
                acc.getUrl().toLowerCase().contains(search) ||
                acc.getUsername().toLowerCase().contains(search) ||
                acc.getTags().toString().toLowerCase().contains(search)
            );
            table.setItems(filtered);
        });

        // Clear Button
        Button clearSearch = new Button("Clear");
        clearSearch.setOnAction(e -> {
            searchField.clear();
            table.setItems(accountList);
        });

        // Filter Button
        Button filterButton = new Button("Filter");
        filterButton.setOnAction(e -> {
            Stage filterStage = new Stage();
            filterStage.initModality(Modality.APPLICATION_MODAL);

            ComboBox<String> tagBox = new ComboBox<>();
            tagBox.getItems().add("All Tags");

            controller.getUser().getAccounts().forEach(acc -> {
                acc.getTags().forEach(tag -> {
                    if (!tagBox.getItems().contains(tag)) {
                        tagBox.getItems().add(tag);
                    }
                });
            });

            tagBox.setValue("All Tags");

            Button apply = new Button("Apply");
            apply.setOnAction(ev -> {
                String tag = tagBox.getValue();
                ObservableList<Account> filtered = accountList.filtered(acc ->
                    tag.equals("All Tags") || acc.getTags().contains(tag)
                );
                table.setItems(filtered);
                filterStage.close();
            });

            VBox box = new VBox(10, new Label("Filter by Tag:"), tagBox, apply);
            box.setPadding(new Insets(20));
            filterStage.setScene(new Scene(box, 300, 200));
            filterStage.showAndWait();
        });

        HBox topBox = new HBox(10, filterButton, searchField, searchButton, clearSearch);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(20, 0, 0, 0));
        
        // CRUD buttons //
        
        // Add Account Button
        Button addPasswordButton = new Button("Add Account");
        addPasswordButton.setGraphic(IconUtil.loadIcon("/images/plus.png", 30));
        addPasswordButton.setOnAction(e -> {
            AddAccount addWindow = new AddAccount();
            addWindow.showWindow(controller);
            loadAccounts(controller);
        });

        // Edit Button
        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> {
            Account selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            EditAccountWindow editWindow = new EditAccountWindow();
            editWindow.showWindow(controller, selected);
            loadAccounts(controller);
        });

        // Delete Button
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> {
            Account selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            controller.deleteAccount(selected);
            loadAccounts(controller);
        });

        // Refresh Button
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadAccounts(controller));

        HBox crudButtons = new HBox(10, addPasswordButton, editButton, deleteButton, refreshButton);
        crudButtons.setAlignment(Pos.CENTER);
        crudButtons.setPadding(new Insets(10));
        
        // Settings button for SubUser
        Button settingsButton = new Button();
        settingsButton.setGraphic(IconUtil.loadIcon("/images/setting.png", 24));
        settingsButton.setOnAction(e -> {
            Settings settingsWindow = new Settings(lanSync);
            settingsWindow.showSettings(controller);
        });

        VBox centerBox = new VBox(10, table, crudButtons);
        centerBox.setPadding(new Insets(20));

        BorderPane mainPane = new BorderPane();
        mainPane.setTop(topBox);
        mainPane.setCenter(centerBox);
        
        HBox bottomBox = new HBox(15, settingsButton);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setPadding(new Insets(20));
        mainPane.setBottom(bottomBox);

        Stage stage = new Stage();
        Scene scene = new Scene(mainPane, 800, 600);
        stage.setScene(scene);
        stage.setTitle("RuneLock - SubUser");
        stage.show();
    }
}
