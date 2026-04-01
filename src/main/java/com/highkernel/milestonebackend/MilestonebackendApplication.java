package com.highkernel.milestonebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MilestonebackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MilestonebackendApplication.class, args);
    }
}