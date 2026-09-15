package ru.codesteps.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message представляет любое сообщение протокола чата.
 * Использует sealed interface с record-классами для типобезопасности.
 */
public sealed interface Message permits
        Message.Auth,
        Message.AuthResult,
        Message.ChatMessage,
        Message.UserConnected,
        Message.UserDisconnected,
        Message.UserList,
        Message.Disconnect {

    Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    /**
     * Auth — запрос аутентификации от клиента.
     */
    record Auth(String login, String password) implements Message {
        public static final String TYPE = "AUTH";
    }

    /**
     * AuthResult — ответ сервера на запрос аутентификации.
     */
    record AuthResult(boolean successful, String message) implements Message {
        public static final String TYPE = "AUTH_RESULT";

        public static AuthResult ok() {
            return new AuthResult(true, "OK");
        }

        public static AuthResult fail(String reason) {
            return new AuthResult(false, reason);
        }

        public boolean success() {
            return successful;
        }
    }

    /**
     * ChatMessage — текстовое сообщение между пользователями.
     */
    record ChatMessage(String from, String to, String text, LocalDateTime timestamp) implements Message {
        public static final String TYPE = "CHAT";

        public ChatMessage(String from, String to, String text) {
            this(from, to, text, LocalDateTime.now());
        }
    }

    /**
     * UserConnected — уведомление о подключении пользователя.
     */
    record UserConnected(String login) implements Message {
        public static final String TYPE = "USER_CONNECTED";
    }

    /**
     * UserDisconnected — уведомление об отключении пользователя.
     */
    record UserDisconnected(String login) implements Message {
        public static final String TYPE = "USER_DISCONNECTED";
    }

    /**
     * UserList — список подключённых пользователей.
     */
    record UserList(java.util.List<String> users) implements Message {
        public static final String TYPE = "USER_LIST";
    }

    /**
     * Disconnect — уведомление о завершении соединения.
     */
    record Disconnect() implements Message {
        public static final String TYPE = "DISCONNECT";
    }

    class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.format(FORMATTER));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            String str = in.nextString();
            return LocalDateTime.parse(str, FORMATTER);
        }
    }
}
