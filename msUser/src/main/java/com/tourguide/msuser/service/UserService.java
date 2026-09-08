package com.tourguide.msuser.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.tourguide.msuser.model.User;

@Service
public class UserService {

    private final List<User> internalUserMap = new ArrayList<>();

    public UserService() {
        initializeInternalUsers();
    }

    private void initializeInternalUsers() {
        for (int i = 0; i < 10; i++) {
            String userName = "internalUser" + i;
            UUID userId = UUID.randomUUID();
            String phone = "000";
            String email = userName + "@tourGuide.com";
            internalUserMap.add(new User(userId, userName, phone, email));
        }
    }

    public List<User> getAllUsers() {
        return internalUserMap;
    }
}