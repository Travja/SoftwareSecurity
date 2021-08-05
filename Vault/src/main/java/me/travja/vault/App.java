package me.travja.vault;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import me.travja.vault.models.*;
import me.travja.vault.models.controls.StyledHBox;
import me.travja.vault.models.controls.StyledVBox;
import me.travja.vault.util.Encryption;
import me.travja.vault.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class App extends Application {

    @Getter
    private static App instance;

    private static String masterPassword;
    private static File passwordsFile = new File("taxes.dat"); //Totally full of tax information....
    @Getter
    private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    @Getter
    private DataFile file = new DataFile(passwordsFile);

    @Getter
    private Button editBtn = new Button("Edit");

    private EntryPanel entryPanel = new EntryPanel();
    private boolean firstStartup;
    private PasswordField passwordField = new PasswordField();
    private TextField searchBar = new TextField();
    private TableView<DataEntry> passwordTable = new TableView<>();
    private TitleScene entryScene = new TitleScene("Add Entry", entryPanel.build(), 640, 480);

    public static void run(String[] args) {
        launch();
    }

//    public static void encrypt() {
//        System.out.print("Enter data: ");
//        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
//            String data = in.readLine();
//            System.out.print("Enter password: ");
//            String password = in.readLine();
//            String decrypted = Encryption.encrypt(data, password);
//            System.out.println("Decrypted: " + decrypted);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static String decryptPassword(String password) {
        return Encryption.decrypt(password, masterPassword);
    }

    public static String encryptPassword(String password) {
        return Encryption.encrypt(password, masterPassword);
    }

    private void initialize() {
        instance = this;
        setupObjectMapper();

        firstStartup = !passwordsFile.exists();
        editBtn.setDisable(true);

        passwordTable.setOnMouseClicked(mouseEvent -> {
            DataEntry selected = passwordTable.getSelectionModel().getSelectedItem();
            editBtn.setDisable(selected == null);
        });

        editBtn.setOnAction(actionEvent -> {
            editBtn.setDisable(true);
            DataEntry selected = passwordTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showAddWindow(selected);
            }
        });

        searchBar.setPromptText("Search...");
        searchBar.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                String searchText = Pattern.quote(searchBar.getText().toLowerCase());

                passwordTable.getItems().clear();
                passwordTable.getItems().addAll(
                        file.getData().stream()
                                .filter(d -> d.getId().toLowerCase().matches(".*" + searchText + ".*"))
                                .collect(Collectors.toList())
                );
            }
        });

        setupTable();
    }

    private void setupTable() {
        TableColumn<DataEntry, String> idCol = new TableColumn("ID");
        idCol.setCellValueFactory(entry -> new SimpleStringProperty(entry.getValue().getId()));
        idCol.prefWidthProperty().bind(passwordTable.widthProperty().divide(4.05));

        TableColumn<DataEntry, String> usernameCol = new TableColumn("Username");
        usernameCol.setCellValueFactory(entry -> new SimpleStringProperty(entry.getValue().getUsername()));
        usernameCol.prefWidthProperty().bind(passwordTable.widthProperty().divide(4.05));

        TableColumn<DataEntry, String> passwordCol = new TableColumn("Password");
        passwordCol.setCellValueFactory(entry -> new SimpleStringProperty(entry.getValue().decodedPassword()));
        passwordCol.prefWidthProperty().bind(passwordTable.widthProperty().divide(4.05));

        TableColumn<DataEntry, String> urlCol = new TableColumn("URL");
        urlCol.setCellValueFactory(entry -> new SimpleStringProperty(entry.getValue().getUrl()));
        urlCol.prefWidthProperty().bind(passwordTable.widthProperty().divide(4.05));

        passwordTable.getColumns().addAll(idCol, usernameCol, passwordCol, urlCol);
    }

    private void setupObjectMapper() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataEntry.class, new DataEntrySerializer());
        mapper.registerModule(module);
        mapper.findAndRegisterModules();
    }

    public void updateList() {
        passwordTable.getItems().clear();
        passwordTable.getItems().addAll(file.getData());
    }

    private void doLogin() {
        masterPassword = passwordField.getText();
        if (firstStartup) {
            System.out.println(masterPassword);
            try {
                passwordsFile.createNewFile();
                file.setMasterPassword(Encryption.sha(masterPassword));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (!checkPassword()) return;

        updateList();

        Button addBtn = new Button("Add Entry");
        addBtn.setOnAction(actionEvent -> showAddWindow());

        HBox hbox = new StyledHBox(addBtn, editBtn)
                .alignment(Pos.CENTER).spacing(10).build();
        HBox searchLine = new StyledHBox(new Label("Search:"), searchBar)
                .alignment(Pos.CENTER).spacing(10).build();
        VBox box = new StyledVBox(searchLine, passwordTable, hbox).spacing(10).alignment(Pos.CENTER)
                .padding(new Insets(10))
                .build();

        TitleScene scene = new TitleScene("Passwords", box, 480, 640);
        Util.showScene(scene);
    }

    private boolean checkPassword() {
        String sha = Encryption.sha(masterPassword);
        if (sha.equals(file.getMasterPassword())) {
            //We're good to go!
            return true;
        } else {
            //Show bad password.
            ObservableList<Node> children = ((VBox) Util.getMainStage().getScene().getRoot()).getChildren();

            if (children.stream().filter(ch -> ch.getId() == "warning").count() >= 1)
                return false;

            List<Node> child = children.stream().collect(Collectors.toList());
            children.clear();
            child.stream().forEach(ch -> {
                if (ch instanceof Button && ch.getId() == "login") {
                    Label warning = new Label("Incorrect password.");
                    warning.setId("warning");
                    children.add(warning);
                }
                children.add(ch);
            });
            return false;
        }
    }

    public void showAddWindow() {
        entryPanel.clear();
        entryScene.setTitle("Add Password");
        Util.newWindow(entryScene);
    }

    public void showAddWindow(DataEntry entry) {
        entryPanel.load(entry);
        entryScene.setTitle("Edit Password");
        Util.newWindow(entryScene);
    }

    @Override
    public void start(Stage stage) {
        initialize();
        Util.setMainStage(stage);

        Label label = new Label("Hello. Welcome to Password Vault 3000.");
        Label password = new Label(firstStartup ? "Please create a Master Password"
                : "Please enter your Master Password");

        Button button = new Button("Click Me.");
        button.setId("login");
        button.setOnAction(actionEvent -> doLogin());

        passwordField.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER)
                doLogin();
        });
        VBox stack = new StyledVBox(label, password, passwordField, button)
                .padding(new Insets(10)).build();


        TitleScene start = new TitleScene("Password Vault 3000", stack, 640, 480);

        Util.showScene(start, stage);
    }

}