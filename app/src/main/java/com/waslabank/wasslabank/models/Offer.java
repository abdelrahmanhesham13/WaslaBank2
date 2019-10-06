package com.waslabank.wasslabank.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.waslabank.wasslabank.models.SingleRequestModel.User;

public class Offer {
    @SerializedName("client")
    @Expose
    private User client;
    @SerializedName("offer")
    @Expose
    private OfferModel offer;

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    public OfferModel getOffer() {
        return offer;
    }

    public void setOffer(OfferModel offer) {
        this.offer = offer;
    }
}
