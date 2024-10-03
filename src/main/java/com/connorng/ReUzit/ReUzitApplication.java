package com.connorng.ReUzit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.connorng.ReUzit")
public class ReUzitApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReUzitApplication.class, args);
	}

}
