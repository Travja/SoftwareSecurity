package me.travja.vault.util;

import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import me.travja.vault.models.TitleScene;

public class Util {

    @Getter
    @Setter
    private static Stage mainStage;

    public static void showScene(Scene scene) {
        showScene(scene, mainStage);
    }

    public static void showScene(Scene scene, Stage stage) {

        if (scene instanceof TitleScene) {
            ((TitleScene) scene).show(stage);
        }

        stage.setScene(scene);
        stage.show();

    }

    public static void newWindow(Scene scene) {
        Stage stage = new Stage();
        showScene(scene, stage);
    }

}
