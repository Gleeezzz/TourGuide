package com.tourguide.msrewards.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import com.tourguide.msrewards.model.Attraction;

@FeignClient(name = "msGpsUtil", url = "http://localhost:8081")
public interface GpsGateway {

    @GetMapping("/attractions")
    ResponseEntity<List<Attraction>> getAttractions();
}
