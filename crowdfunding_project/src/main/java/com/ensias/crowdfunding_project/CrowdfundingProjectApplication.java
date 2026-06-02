package com.ensias.crowdfunding_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CrowdfundingProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrowdfundingProjectApplication.class, args);
    }
}