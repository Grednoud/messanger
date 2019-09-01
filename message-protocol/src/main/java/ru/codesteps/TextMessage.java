package ru.codesteps;

import java.time.LocalDateTime;

public class TextMessage {
    private LocalDateTime created;

    private String userFrom;
    private String userTo;
    private String text;

    public TextMessage(String userFrom, String userTo, String text) {
        this(userFrom, userTo, text, LocalDateTime.now());
    }

    public TextMessage(String userFrom, String userTo, String text, LocalDateTime created) {
        this.userFrom = userFrom;
        this.userTo = userTo;
        this.text = text;
        this.created = created;
    }

    public void swapUsers() {
        String tmp = userFrom;
        userFrom = userTo;
        userTo = tmp;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public String getUserFrom() {
        return userFrom;
    }

    public void setUserFrom(String userFrom) {
        this.userFrom = userFrom;
    }

    public String getUserTo() {
        return userTo;
    }

    public void setUserTo(String userTo) {
        this.userTo = userTo;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
