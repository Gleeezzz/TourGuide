package com.tourguide.msrewards.model;

public class Attraction {

    private double longitude;
    private double latitude;
    private String attractionName;
    private String city;
    private String state;
    private String attractionId;

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getAttractionId() {
        return attractionId;
    }
}