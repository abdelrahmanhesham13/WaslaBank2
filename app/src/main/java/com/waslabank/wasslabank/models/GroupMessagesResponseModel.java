package com.waslabank.wasslabank.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GroupMessagesResponseModel {
    @SerializedName("messages")
    @Expose
    private List<GroupMessageModel> messages = null;
    @SerializedName("members")
    @Expose
    private List<MemberModel> members = null;
    @SerializedName("status")
    @Expose
    private Boolean status;

    public List<GroupMessageModel> getMessages() {
        return messages;
    }

    public void setMessages(List<GroupMessageModel> messages) {
        this.messages = messages;
    }

    public List<MemberModel> getMembers() {
        return members;
    }

    public void setMembers(List<MemberModel> members) {
        this.members = members;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
