package com.waslabank.wasslabank.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestModel {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("city_id")
    @Expose
    private String cityId;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("longitude")
    @Expose
    private String longitude;
    @SerializedName("latitude")
    @Expose
    private String latitude;
    @SerializedName("longitude_to")
    @Expose
    private String longitudeTo;
    @SerializedName("latitude_to")
    @Expose
    private String latitudeTo;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("created")
    @Expose
    private String created;
    @SerializedName("updated")
    @Expose
    private String updated;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("address_to")
    @Expose
    private String addressTo;
    @SerializedName("city_id_to")
    @Expose
    private String cityIdTo;
    @SerializedName("request_time")
    @Expose
    private String requestTime;
    @SerializedName("views")
    @Expose
    private String views;
    @SerializedName("request_date")
    @Expose
    private String requestDate;
    @SerializedName("duration")
    @Expose
    private String duration;
    @SerializedName("longitude_update")
    @Expose
    private String longitudeUpdate;
    @SerializedName("latitude_update")
    @Expose
    private String latitudeUpdate;
    @SerializedName("from_id")
    @Expose
    private String fromId;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("seats")
    @Expose
    private String seats;
    @SerializedName("secondary_id")
    @Expose
    private String secondaryId;
    @SerializedName("distance")
    @Expose
    private String distance;
    @SerializedName("start")
    @Expose
    private String start;
    @SerializedName("picked")
    @Expose
    private String picked;
    @SerializedName("user_longitude")
    @Expose
    private String userLongitude;
    @SerializedName("user_latitude")
    @Expose
    private String userLatitude;
    @SerializedName("user")
    @Expose
    private UserModel user;
    @SerializedName("from")
    @Expose
    private UserModel from;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitudeTo() {
        return longitudeTo;
    }

    public void setLongitudeTo(String longitudeTo) {
        this.longitudeTo = longitudeTo;
    }

    public String getLatitudeTo() {
        return latitudeTo;
    }

    public void setLatitudeTo(String latitudeTo) {
        this.latitudeTo = latitudeTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAddressTo() {
        return addressTo;
    }

    public void setAddressTo(String addressTo) {
        this.addressTo = addressTo;
    }

    public String getCityIdTo() {
        return cityIdTo;
    }

    public void setCityIdTo(String cityIdTo) {
        this.cityIdTo = cityIdTo;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLongitudeUpdate() {
        return longitudeUpdate;
    }

    public void setLongitudeUpdate(String longitudeUpdate) {
        this.longitudeUpdate = longitudeUpdate;
    }

    public String getLatitudeUpdate() {
        return latitudeUpdate;
    }

    public void setLatitudeUpdate(String latitudeUpdate) {
        this.latitudeUpdate = latitudeUpdate;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSeats() {
        return seats;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public String getSecondaryId() {
        return secondaryId;
    }

    public void setSecondaryId(String secondaryId) {
        this.secondaryId = secondaryId;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getPicked() {
        return picked;
    }

    public void setPicked(String picked) {
        this.picked = picked;
    }

    public String getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(String userLongitude) {
        this.userLongitude = userLongitude;
    }

    public String getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(String userLatitude) {
        this.userLatitude = userLatitude;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public UserModel getFrom() {
        return from;
    }

    public void setFrom(UserModel from) {
        this.from = from;
    }
}
