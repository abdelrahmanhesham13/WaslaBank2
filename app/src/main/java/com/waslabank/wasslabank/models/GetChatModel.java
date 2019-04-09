package com.waslabank.wasslabank.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetChatModel {
    @SerializedName("chat_id")
    @Expose
    private String chatId;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("to_id")
    @Expose
    private String toId;
    @SerializedName("from_id")
    @Expose
    private String fromId;
    @SerializedName("type")
    @Expose
    private Object type;
    @SerializedName("date")
    @Expose
    private String date;

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public Object getType() {
        return type;
    }

    public void setType(Object type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
