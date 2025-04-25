package com.stationery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class StationeryApplication {

    public static void main(String[] args) {
        SpringApplication.run(StationeryApplication.class, args);
    }
}