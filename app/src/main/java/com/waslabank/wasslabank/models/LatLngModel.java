package com.waslabank.wasslabank.models;

public class LatLngModel {

    String Lat,Lng;

    public LatLngModel() {
    }

    public String getLat() {
        return Lat;
    }

    public void setLat(String lat) {
        Lat = lat;
    }

    public String getLng() {
        return Lng;
    }

    public void setLng(String lng) {
        Lng = lng;
    }

    public LatLngModel(String lat, String lng) {
        Lat = lat;
        Lng = lng;
    }
}
