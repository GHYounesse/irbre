package com.example.test_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestAppApplication {
	public static void main(String[] args) {
		System.out.println(">>> SPRING BOOT STARTING <<<");
		SpringApplication app = new SpringApplication(TestAppApplication.class);
		app.setLogStartupInfo(true);
		app.run(args);
		System.out.println(">>> SPRING BOOT STARTED <<<");
	}
}


