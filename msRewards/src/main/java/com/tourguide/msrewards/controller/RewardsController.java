package com.tourguide.msrewards.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tourguide.msrewards.client.GpsUtilClient;
import com.tourguide.msrewards.model.Attraction;

@RestController
public class RewardsController {

    @Autowired
    private GpsUtilClient gpsUtilClient;

    @GetMapping("/test-attractions")
    public List<Attraction> testGetAttractions() {
        return gpsUtilClient.getAttractions();
    }
}