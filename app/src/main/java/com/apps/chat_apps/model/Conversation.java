package com.apps.chat_apps.model;

public class Conversation {
    private final String id;
    private final User otherUser;
    private final Message lastMessage;

    public Conversation(String id, User otherUser, Message lastMessage) {
        this.id = id;
        this.otherUser = otherUser;
        this.lastMessage = lastMessage;
    }

    public String getId() {
        return id;
    }

    public User getOtherUser() {
        return otherUser;
    }

    public Message getLastMessage() {
        return lastMessage;
    }
}
