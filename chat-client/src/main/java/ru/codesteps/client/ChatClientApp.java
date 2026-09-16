package ru.codesteps.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.codesteps.client.ui.MainView;

/**
 * ChatClientApp — точка входа клиента чата.
 */
public final class ChatClientApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        var mainView = new MainView(primaryStage);
        var scene = new Scene(mainView, 900, 600);

        scene.getStylesheets().add(
                getClass().getResource("/css/dark-theme.css").toExternalForm()
        );

        primaryStage.setTitle("Сетевой чат / Network Chat");
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> mainView.shutdown());
        primaryStage.show();

        mainView.initialize();
    }
}
