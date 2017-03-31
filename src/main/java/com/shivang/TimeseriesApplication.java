package com.shivang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TimeseriesApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(TimeseriesApplication.class, args);
    }

}
