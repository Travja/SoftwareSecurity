package me.travja.vault.models;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import me.travja.vault.App;
import me.travja.vault.models.controls.StyledHBox;
import me.travja.vault.models.controls.StyledVBox;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class EntryPanel {

    @Getter
    public final TextField id = new TextField(),
            username = new TextField(),
            password = new TextField(),
            url = new TextField();

    @Getter
    public final Label idLabel = new Label("Entry Id:"),
            usernameLabel = new Label("Username:"),
            passwordLabel = new Label("Password:"),
            urlLabel = new Label("URL:");

    @Getter
    public final Button submitBtn = new Button("Submit");
    @Getter
    public VBox box;
    private boolean isEdit = false;

    public VBox build() {
        idLabel.setMinWidth(100);
        usernameLabel.setMinWidth(100);
        passwordLabel.setMinWidth(100);
        urlLabel.setMinWidth(100);

        VBox box = new StyledVBox(new StyledHBox(idLabel, id).spacing(20).alignment(Pos.CENTER).build(),
                new StyledHBox(usernameLabel, username).spacing(20).alignment(Pos.CENTER).build(),
                new StyledHBox(passwordLabel, password).spacing(20).alignment(Pos.CENTER).build(),
                new StyledHBox(urlLabel, url).spacing(20).alignment(Pos.CENTER).build(),
                new StyledHBox(submitBtn).spacing(20).alignment(Pos.CENTER).id("submit").build())
                .spacing(10)
                .build();
        this.box = box;

        submitBtn.setOnAction(eventHandler -> {
            System.out.println("Clicked on submit. Adding information.");
            //TODO Verify that there is indeed data in the fields.

            if (isEdit) {
                App app = App.getInstance();
                Optional<DataEntry> data = app.getFile().getData()
                        .stream().filter(d -> d.getId().equals(getIdText())).findFirst();

                data.ifPresent(dat -> {
                    dat.setUsername(getUsernameText());
                    dat.setPassword(app.encryptPassword(getPasswordText()));
                    dat.setUrl(getURLText());
                });

                app.getFile().save();
                app.updateList();
                ((Stage) box.getScene().getWindow()).close();
                return;
            }

            DataEntry entry = new DataEntry(getIdText(), getUsernameText(), getPasswordText(), getURLText());

            DataFile file = App.getInstance().getFile();
            if (file.getEntry(entry.getId()) != null) {
                // We already have an entry with this id... But maybe that's okay?
                showWarning("There is already an entry with this id.");
                return;
            }
            file.addEntry(entry);
            file.save();
            ((Stage) box.getScene().getWindow()).close();
        });

        box.setPadding(new Insets(10));

        return box;
    }

    private void showWarning(String warn) {
        ObservableList<Node> children = box.getChildren();

        if (children.stream().filter(ch -> ch.getId() == "warning").count() >= 1)
            return;

        List<Node> child = children.stream().collect(Collectors.toList());
        children.clear();
        child.stream().forEach(ch -> {
            if (ch.getId() != null && ch.getId().equals("submit")) {
                Label warning = new Label(warn);
                HBox b = new StyledHBox(warning).alignment(Pos.CENTER).id("warning").build();
                children.add(b);
            }
            children.add(ch);
        });
    }

    public void clear() {
        isEdit = false;
        id.setEditable(true);
        id.setText("");
        username.setText("");
        password.setText("");
        url.setText("");

        box.getChildren().removeIf(n -> n.getId() != null && n.getId().equals("warning"));
    }

    public void load(DataEntry entry) {
        isEdit = true;
        id.setEditable(false);
        id.setFocusTraversable(false);
        id.setText(entry.getId());
        username.setText(entry.getUsername());
        password.setText(entry.decodedPassword());
        url.setText(entry.getUrl());

        box.getChildren().removeIf(n -> n.getId() != null && n.getId().equals("warning"));
    }

    public String getIdText() {
        return id.getText();
    }

    public void setIdText(String text) {
        id.setText(text);
    }

    public String getUsernameText() {
        return username.getText();
    }

    public void setUsernameText(String text) {
        username.setText(text);
    }

    public String getPasswordText() {
        return password.getText();
    }

    public void setPasswordText(String text) {
        password.setText(text);
    }

    public String getURLText() {
        return url.getText();
    }

    public void setURLText(String text) {
        url.setText(text);
    }

}
