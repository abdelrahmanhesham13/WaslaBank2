package com.waslabank.wasslabank.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GroupsResponseModel {

    @SerializedName("datas")
    @Expose
    private List<GroupChatModel> groups = null;
    @SerializedName("status")
    @Expose
    private Boolean status;

    public List<GroupChatModel> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupChatModel> groups) {
        this.groups = groups;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
