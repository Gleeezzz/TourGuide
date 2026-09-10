package com.tourguide.msrewards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsRewardsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsRewardsApplication.class, args);
    }
}