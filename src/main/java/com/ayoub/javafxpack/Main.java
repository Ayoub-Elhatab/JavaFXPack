package com.ayoub.javafxpack;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ayoub/javafxpack/fxml/main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 960, 910);

        stage.setTitle("JavaFXPack");
        stage.setScene(scene);
        stage.setMinWidth(860);
        stage.setMinHeight(560);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
