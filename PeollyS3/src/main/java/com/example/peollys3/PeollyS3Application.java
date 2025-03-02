package com.example.peollys3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PeollyS3Application {
    public static void main(String[] args) {
        SpringApplication.run(PeollyS3Application.class, args);
    }
}
