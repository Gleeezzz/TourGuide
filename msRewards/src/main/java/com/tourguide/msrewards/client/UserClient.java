package com.tourguide.msrewards.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "msUser", url = "http://localhost:8082")
public interface UserClient {

    @GetMapping("/users")
    List<Object> getAllUsers(); // on affinera le DTO User plus tard
}