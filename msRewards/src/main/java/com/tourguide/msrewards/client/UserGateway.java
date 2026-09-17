package com.tourguide.msrewards.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import com.tourguide.msrewards.model.User;

@FeignClient(name = "msUser", url = "http://localhost:8082")
public interface UserGateway {

    @GetMapping("/users")
    ResponseEntity<List<User>> getAllUsers();
}