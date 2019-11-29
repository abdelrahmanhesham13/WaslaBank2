package com.waslabank.wasslabank.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.waslabank.wasslabank.models.SingleRequestModel.User;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class UpdateToken {
    @SerializedName("status")
    @Expose
    private Boolean status;
    @SerializedName("active")
    @Expose
    private Boolean active;
    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("orders")
    @Expose
    private Integer orders;
    @SerializedName("comments")
    @Expose
    private Integer comments;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("request_id")
    @Expose
    private String requestId;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getOrders() {
        return orders;
    }

    public void setOrders(Integer orders) {
        this.orders = orders;
    }

    public Integer getComments() {
        return comments;
    }

    public void setComments(Integer comments) {
        this.comments = comments;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }


    @NonNull
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("status", status).append("active", active).append("user", user).append("orders", orders).append("comments", comments).append("message", message).append("requestId", requestId).toString();
    }
}
