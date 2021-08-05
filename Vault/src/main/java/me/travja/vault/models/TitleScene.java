package me.travja.vault.models;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

public class TitleScene extends Scene {

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private Scene previous;

    @Getter
    @Setter
    private String prevTitle;

    @Getter
    private Stage stage;

    public TitleScene(String title, Parent parent) {
        super(parent);
        this.title = title;
    }

    public TitleScene(String title, Parent parent, double width, double height) {
        super(parent, width, height);
        this.title = title;
    }

    public TitleScene title(String title) {
        this.title = title;
        return this;
    }

    public void show(Stage stage) {
        this.stage = stage;
        prevTitle = stage.getTitle();
        previous = stage.getScene();

        stage.setWidth(this.getWidth());
        stage.setHeight(this.getHeight());

        if (title != null)
            stage.setTitle(title);

        stage.setScene(this);
        stage.show();
    }

}
