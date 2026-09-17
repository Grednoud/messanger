package ru.codesteps.protocol;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Protocol обеспечивает сериализацию/десериализацию сообщений в JSON.
 * Формат: {"type": "MESSAGE_TYPE", "data": {...}}
 */
public final class Protocol {

    private Protocol() {
    }

    /**
     * Сериализует сообщение в JSON-строку.
     *
     * @param message сообщение для сериализации
     * @return JSON-строка
     */
    public static String serialize(Message message) {
        JsonObject json = new JsonObject();
        json.addProperty("type", getMessageType(message));
        json.add("data", Message.GSON.toJsonTree(message));
        return json.toString();
    }

    /**
     * Десериализует JSON-строку в сообщение.
     *
     * @param json JSON-строка
     * @return сообщение или null при ошибке парсинга
     */
    public static Message deserialize(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            String type = obj.get("type").getAsString();
            JsonObject data = obj.getAsJsonObject("data");

            return switch (type) {
                case Message.Auth.TYPE -> Message.GSON.fromJson(data, Message.Auth.class);
                case Message.AuthResult.TYPE -> Message.GSON.fromJson(data, Message.AuthResult.class);
                case Message.ChatMessage.TYPE -> Message.GSON.fromJson(data, Message.ChatMessage.class);
                case Message.UserConnected.TYPE -> Message.GSON.fromJson(data, Message.UserConnected.class);
                case Message.UserDisconnected.TYPE -> Message.GSON.fromJson(data, Message.UserDisconnected.class);
                case Message.UserList.TYPE -> Message.GSON.fromJson(data, Message.UserList.class);
                case Message.Disconnect.TYPE -> new Message.Disconnect();
                default -> null;
            };
        } catch (JsonSyntaxException | NullPointerException | IllegalStateException e) {
            return null;
        }
    }

    private static String getMessageType(Message message) {
        return switch (message) {
            case Message.Auth auth -> Message.Auth.TYPE;
            case Message.AuthResult result -> Message.AuthResult.TYPE;
            case Message.ChatMessage chat -> Message.ChatMessage.TYPE;
            case Message.UserConnected connected -> Message.UserConnected.TYPE;
            case Message.UserDisconnected disconnected -> Message.UserDisconnected.TYPE;
            case Message.UserList list -> Message.UserList.TYPE;
            case Message.Disconnect disconnect -> Message.Disconnect.TYPE;
        };
    }
}
