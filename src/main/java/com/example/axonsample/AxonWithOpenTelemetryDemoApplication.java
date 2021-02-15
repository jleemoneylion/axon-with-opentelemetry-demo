package com.example.axonsample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AxonWithOpenTelemetryDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AxonWithOpenTelemetryDemoApplication.class, args);
    }

}
