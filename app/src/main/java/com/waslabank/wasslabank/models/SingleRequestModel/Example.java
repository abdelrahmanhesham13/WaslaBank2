
package com.waslabank.wasslabank.models.SingleRequestModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Example {

    @SerializedName("request")
    @Expose
    private Request request;
    @SerializedName("status")
    @Expose
    private Boolean status;

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

}
