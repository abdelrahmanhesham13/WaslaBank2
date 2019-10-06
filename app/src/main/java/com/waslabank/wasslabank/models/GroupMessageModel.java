package com.waslabank.wasslabank.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GroupMessageModel {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("group_id")
    @Expose
    private String groupId;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("created")
    @Expose
    private String created;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

}
