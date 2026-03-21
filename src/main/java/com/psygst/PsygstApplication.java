package com.psygst;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PsygstApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(PsygstApplication.class, args);
    }
}
