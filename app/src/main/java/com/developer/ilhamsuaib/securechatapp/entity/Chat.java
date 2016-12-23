package com.developer.ilhamsuaib.securechatapp.entity;

/**
 * Created by ilham on 07/12/2016.
 */

public class Chat {
    private String time;
    private String message;
    private String username;
    private MessageType messageType;

    public enum MessageType{Text, Audio}

    public Chat() {
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
