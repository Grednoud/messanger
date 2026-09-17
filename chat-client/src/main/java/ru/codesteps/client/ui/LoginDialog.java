package ru.codesteps.client.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * LoginDialog — диалог подключения к серверу.
 */
public final class LoginDialog extends Dialog<LoginDialog.Credentials> {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 11111;

    private final TextField hostField;
    private final Spinner<Integer> portSpinner;
    private final TextField loginField;
    private final PasswordField passwordField;

    public LoginDialog(Stage owner) {
        bindOwnerIfReady(owner);
        setTitle("Подключение к чату");
        setHeaderText("Тестовые пользователи: Alex / 123, Bob / 234, Clod / 345");

        var connectButtonType = new ButtonType("Подключиться", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);
        getDialogPane().getStyleClass().add("login-dialog");

        var grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        hostField = new TextField(DEFAULT_HOST);
        hostField.setPromptText("Адрес сервера");
        hostField.setPrefWidth(200);

        portSpinner = new Spinner<>();
        portSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1024, 65535, DEFAULT_PORT
        ));
        portSpinner.setEditable(true);
        portSpinner.setPrefWidth(100);

        loginField = new TextField();
        loginField.setPromptText("Логин");

        passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");

        grid.add(new Label("Сервер:"), 0, 0);
        grid.add(hostField, 1, 0);
        grid.add(new Label("Порт:"), 2, 0);
        grid.add(portSpinner, 3, 0);

        grid.add(new Label("Логин:"), 0, 1);
        grid.add(loginField, 1, 1, 3, 1);

        grid.add(new Label("Пароль:"), 0, 2);
        grid.add(passwordField, 1, 2, 3, 1);

        getDialogPane().setContent(grid);

        var connectButton = (Button) getDialogPane().lookupButton(connectButtonType);
        connectButton.setDisable(true);

        loginField.textProperty().addListener((obs, oldVal, newValue) ->
                connectButton.setDisable(newValue.isBlank() || passwordField.getText().isBlank())
        );
        passwordField.textProperty().addListener((obs, oldVal, newValue) ->
                connectButton.setDisable(newValue.isBlank() || loginField.getText().isBlank())
        );

        setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {
                return new Credentials(
                        hostField.getText().trim(),
                        portSpinner.getValue(),
                        loginField.getText().trim(),
                        passwordField.getText()
                );
            }
            return null;
        });

        loginField.requestFocus();
    }

    /**
     * JavaFX 21 {@code Dialog.initOwner()} throws NPE when the owner Stage
     * has no Scene yet (HeavyweightDialog.updateStageBindings).
     */
    public static boolean canBindOwner(Stage owner) {
        return owner != null && owner.getScene() != null;
    }

    private void bindOwnerIfReady(Stage owner) {
        if (canBindOwner(owner)) {
            initOwner(owner);
        }
    }

    /**
     * Credentials содержит данные для подключения.
     */
    public record Credentials(String host, int port, String login, String password) {
    }
}
