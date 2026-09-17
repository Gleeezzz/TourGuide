package com.tourguide.msrewards.model;

import java.util.UUID;

public class User {

    private UUID userId;
    private String userName;
    private String phoneNumber;
    private String emailAddress;

    public User() {
        // constructeur vide nécessaire pour la désérialisation JSON
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }
}
