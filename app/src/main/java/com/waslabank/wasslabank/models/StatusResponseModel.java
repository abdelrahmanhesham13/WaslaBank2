package com.waslabank.wasslabank.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StatusResponseModel {
    @SerializedName("status")
    @Expose
    private Boolean status;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
