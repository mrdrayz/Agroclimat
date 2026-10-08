package com.agroclimate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgroClimateApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgroClimateApplication.class, args);
    }
}
