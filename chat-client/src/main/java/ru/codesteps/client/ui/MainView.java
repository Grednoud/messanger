package ru.codesteps.client.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.codesteps.client.NetworkClient;
import ru.codesteps.protocol.Message;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MainView — основной интерфейс клиента чата.
 */
public final class MainView extends BorderPane {

    private static final Logger LOG = Logger.getLogger(MainView.class.getName());
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final Stage stage;
    private final ObservableList<String> users = FXCollections.observableArrayList();
    private final ObservableList<ChatEntry> messages = FXCollections.observableArrayList();

    private NetworkClient client;
    private ListView<String> userListView;
    private ListView<ChatEntry> messageListView;
    private TextField messageInput;
    private Button sendButton;
    private Label statusLabel;

    public MainView(Stage stage) {
        this.stage = stage;
        getStyleClass().add("main-view");
        buildUI();
    }

    /**
     * Инициализирует диалог подключения. Вызывать после установки Scene на Stage
     * и показа окна — иначе JavaFX Dialog.initOwner() падает с NPE.
     */
    public void initialize() {
        Platform.runLater(this::showLoginDialog);
    }

    private void buildUI() {
        setLeft(buildUserList());
        setCenter(buildChatArea());
        setBottom(buildStatusBar());
    }

    private VBox buildUserList() {
        var header = new Label("Пользователи");
        header.getStyleClass().add("section-header");

        userListView = new ListView<>(users);
        userListView.getStyleClass().add("user-list");
        userListView.setPrefWidth(180);
        VBox.setVgrow(userListView, Priority.ALWAYS);

        var container = new VBox(header, userListView);
        container.getStyleClass().add("user-panel");
        container.setPadding(new Insets(10));
        container.setSpacing(8);
        return container;
    }

    private VBox buildChatArea() {
        var header = new Label("Сообщения");
        header.getStyleClass().add("section-header");

        messageListView = new ListView<>(messages);
        messageListView.getStyleClass().add("message-list");
        messageListView.setCellFactory(list -> new MessageCell());
        VBox.setVgrow(messageListView, Priority.ALWAYS);

        var inputArea = buildInputArea();

        var container = new VBox(header, messageListView, inputArea);
        container.getStyleClass().add("chat-panel");
        container.setPadding(new Insets(10));
        container.setSpacing(8);
        return container;
    }

    private HBox buildInputArea() {
        messageInput = new TextField();
        messageInput.setPromptText("Введите сообщение...");
        messageInput.getStyleClass().add("message-input");
        messageInput.setDisable(true);
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        messageInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        sendButton = new Button("Отправить");
        sendButton.getStyleClass().add("send-button");
        sendButton.setDisable(true);
        sendButton.setOnAction(e -> sendMessage());

        var container = new HBox(messageInput, sendButton);
        container.setSpacing(8);
        container.setAlignment(Pos.CENTER_LEFT);
        return container;
    }

    private HBox buildStatusBar() {
        statusLabel = new Label("Не подключено");
        statusLabel.getStyleClass().add("status-label");

        var container = new HBox(statusLabel);
        container.getStyleClass().add("status-bar");
        container.setPadding(new Insets(8, 10, 8, 10));
        return container;
    }

    private void showLoginDialog() {
        var dialog = new LoginDialog(stage);
        dialog.showAndWait().ifPresent(credentials -> {
            connectToServer(credentials.host(), credentials.port(),
                    credentials.login(), credentials.password());
        });
    }

    private void connectToServer(String host, int port, String login, String password) {
        client = new NetworkClient(host, port, this::handleMessage);

        Thread.ofVirtual().start(() -> {
            try {
                var result = client.connect(login, password);
                Platform.runLater(() -> {
                    if (result.success()) {
                        onConnected(login);
                    } else {
                        onConnectionFailed(result.errorMessage());
                    }
                });
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Ошибка подключения: {0}", e.getMessage());
                Platform.runLater(() -> onConnectionFailed("Не удалось подключиться к серверу"));
            }
        });
    }

    private void onConnected(String login) {
        statusLabel.setText("Подключено как: " + login);
        statusLabel.getStyleClass().add("connected");
        messageInput.setDisable(false);
        sendButton.setDisable(false);
        messageInput.requestFocus();
    }

    private void onConnectionFailed(String error) {
        statusLabel.setText("Ошибка: " + error);

        var errorDialog = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
        );
        errorDialog.setTitle("Ошибка подключения");
        errorDialog.setHeaderText("Не удалось подключиться");
        errorDialog.setContentText(error);
        errorDialog.showAndWait();

        showLoginDialog();
    }

    private void handleMessage(Message message) {
        Platform.runLater(() -> {
            switch (message) {
                case Message.ChatMessage chat -> {
                    messages.add(new ChatEntry(
                            chat.from(),
                            chat.text(),
                            chat.timestamp().format(TIME_FORMAT),
                            false
                    ));
                    messageListView.scrollTo(messages.size() - 1);
                }
                case Message.UserConnected event -> {
                    if (!users.contains(event.login())) {
                        users.add(event.login());
                    }
                    messages.add(new ChatEntry(
                            null,
                            event.login() + " подключился",
                            "",
                            true
                    ));
                }
                case Message.UserDisconnected event -> {
                    users.remove(event.login());
                    messages.add(new ChatEntry(
                            null,
                            event.login() + " отключился",
                            "",
                            true
                    ));
                }
                case Message.UserList list -> {
                    users.clear();
                    users.addAll(list.users());
                }
                default -> LOG.log(Level.FINE, "Неизвестное сообщение: {0}", message);
            }
        });
    }

    private void sendMessage() {
        String text = messageInput.getText().trim();
        String recipient = userListView.getSelectionModel().getSelectedItem();

        if (text.isEmpty()) {
            return;
        }

        if (recipient == null || recipient.equals(client.getLogin())) {
            statusLabel.setText("Выберите получателя из списка");
            return;
        }

        client.sendMessage(recipient, text);

        messages.add(new ChatEntry(
                "Вы → " + recipient,
                text,
                java.time.LocalTime.now().format(TIME_FORMAT),
                false
        ));
        messageListView.scrollTo(messages.size() - 1);

        messageInput.clear();
        messageInput.requestFocus();
    }

    public void shutdown() {
        if (client != null) {
            client.close();
        }
    }

    /**
     * ChatEntry представляет одну запись в списке сообщений.
     */
    record ChatEntry(String sender, String text, String time, boolean isSystem) {
    }

    /**
     * MessageCell отображает одно сообщение в списке.
     */
    private static final class MessageCell extends ListCell<ChatEntry> {
        @Override
        protected void updateItem(ChatEntry entry, boolean empty) {
            super.updateItem(entry, empty);

            if (empty || entry == null) {
                setText(null);
                setGraphic(null);
                getStyleClass().removeAll("system-message", "chat-message");
            } else {
                var container = new VBox();
                container.setSpacing(2);

                if (entry.isSystem()) {
                    var label = new Label(entry.text());
                    label.getStyleClass().add("system-text");
                    container.getChildren().add(label);
                    getStyleClass().add("system-message");
                } else {
                    var header = new HBox();
                    header.setSpacing(8);

                    var sender = new Label(entry.sender());
                    sender.getStyleClass().add("message-sender");

                    var time = new Label(entry.time());
                    time.getStyleClass().add("message-time");

                    header.getChildren().addAll(sender, time);

                    var text = new Label(entry.text());
                    text.getStyleClass().add("message-text");
                    text.setWrapText(true);

                    container.getChildren().addAll(header, text);
                    getStyleClass().add("chat-message");
                }

                setGraphic(container);
            }
        }
    }
}
