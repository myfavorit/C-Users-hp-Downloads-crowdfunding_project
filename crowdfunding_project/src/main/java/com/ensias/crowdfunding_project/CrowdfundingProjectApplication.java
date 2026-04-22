package com.ensias.crowdfunding_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class CrowdfundingProjectApplication {
	public static void main(String[] args) {
		SpringApplication.run(CrowdfundingProjectApplication.class, args);
	}
}