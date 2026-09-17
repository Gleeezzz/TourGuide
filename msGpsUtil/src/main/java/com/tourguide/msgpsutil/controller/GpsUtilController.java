package com.tourguide.msgpsutil.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;

@RestController
public class GpsUtilController {

    private final GpsUtil gpsUtil = new GpsUtil();

    @GetMapping("/attractions")
    public List<Attraction> getAttractions() {
        return gpsUtil.getAttractions();
    }
}
