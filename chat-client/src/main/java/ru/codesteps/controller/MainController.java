package ru.codesteps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import ru.codesteps.ChatHistory;
import ru.codesteps.MessageReciever;
import ru.codesteps.Network;
import ru.codesteps.TextMessage;
import ru.codesteps.exception.AuthException;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

public class MainController implements Initializable, MessageReciever {
    @FXML
    public TextField tfMessage;

    @FXML
    public ListView<TextMessage> lvMessages;

    @FXML
    public Button btSendMessage;

    @FXML
    public TextField loginField;

    @FXML
    public PasswordField passField;

    @FXML
    public HBox authPanel;

    @FXML
    public HBox msgPanel;

    @FXML
    public ListView<String> lvUserList;

    Stage primaryStage;

    private ObservableList<TextMessage> messageList;

    private ObservableList<String> userList;

    private Network network;
    private ChatHistory chatHistory;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messageList = FXCollections.observableArrayList();

        lvMessages.setItems(messageList);
        lvMessages.setCellFactory(new Callback<ListView<TextMessage>, ListCell<TextMessage>>() {
            @Override
            public ListCell<TextMessage> call(ListView<TextMessage> param) {
                return new MessageCellController();
            }
        });

        userList = FXCollections.observableArrayList();
        userList.addAll("Alex", "Bob", "Clod");
        lvUserList.setItems(userList);

        network = new Network("localhost", 11111, this);
        authPanel.setVisible(true);
        msgPanel.setVisible(false);
    }

    public void onSendMessageClicked() {
        String text = tfMessage.getText();
        if (text != null && !text.isEmpty()) {
            String userTo = lvUserList.getSelectionModel().getSelectedItem();
            TextMessage msg = new TextMessage(network.getLogin(), userTo, text);
            messageList.add(msg);
            tfMessage.clear();
            tfMessage.requestFocus();

            network.sendTextMessage(msg);
        }
    }

    public void sendAuth() {
        try {
            network.authorize(loginField.getText(), passField.getText());
        } catch (AuthException ex) {
            ex.printStackTrace();
            showModalAlert("Сетевой чат",
                    "Авторизация",
                    "Ошибка авторизации",
                    Alert.AlertType.ERROR);
            return;
        } catch (IOException ex) {
            ex.printStackTrace();
            showModalAlert("Сетевой чат",
                    "Авторизация",
                    "Ошибка сети",
                    Alert.AlertType.ERROR);
            return;
        }
        authPanel.setVisible(false);
        msgPanel.setVisible(true);
    }

    public void closeNetworkConnection() {
        network.close();
    }

    public void sendAuthMessage() {
        try {
            network.authorize(loginField.getText(), passField.getText());
        } catch (AuthException e) {
            e.printStackTrace();
            showModalAlert("Network chat", "Authentication", "Authentication error!", Alert.AlertType.ERROR);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            showModalAlert("Network chat", "Authentication", "Network error!", Alert.AlertType.ERROR);
        }

        authPanel.setVisible(false);
        msgPanel.setVisible(true);
    }

    private static void showModalAlert(String title, String header, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);

        alert.showAndWait();
    }

    @Override
    public void submitMessage(TextMessage message) {

    }

    @Override
    public void userConnected(String login) {

    }

    @Override
    public void userDisconnected(String login) {

    }

    @Override
    public void updateUserList(Set<String> users) {

    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
