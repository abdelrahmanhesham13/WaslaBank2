package com.waslabank.wasslabank.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class StatusModel {

    @SerializedName("request")
    @Expose
    private RequestModel request;
    @SerializedName("status")
    @Expose
    private Boolean status;
    @SerializedName("user")
    @Expose
    private UserModel user;
    @SerializedName("chats")
    @Expose
    private ArrayList<GetChatModel> getChatModel;

    public ArrayList<GetChatModel> getGetChatModel() {
        return getChatModel;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public RequestModel getRequest() {
        return request;
    }

    public void setRequest(RequestModel request) {
        this.request = request;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

}
