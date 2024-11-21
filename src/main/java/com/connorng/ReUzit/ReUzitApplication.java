package com.connorng.ReUzit;

import com.connorng.ReUzit.controller.listing.ListingRequest;
import com.connorng.ReUzit.model.Image;
import com.connorng.ReUzit.model.Listing;
import com.connorng.ReUzit.s3.S3Buckets;
import com.connorng.ReUzit.s3.S3Service;
import com.connorng.ReUzit.service.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@SpringBootApplication
@ComponentScan(basePackages = "com.connorng.ReUzit")
public class ReUzitApplication {

	@Autowired
	private ListingService listingService;
	public static void main(String[] args) {
		SpringApplication.run(ReUzitApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(S3Service s3Service, S3Buckets s3Buckets) {
		return args -> {
		};
	}

	private static void testS3Buckets(S3Service s3Service, S3Buckets s3Buckets) {
		s3Service.putObject(s3Buckets.getListing(), "foo", "Hello world".getBytes());

		byte[] obj = s3Service.getObject(s3Buckets.getListing(), "foo");

		System.out.println("hooray: " + new String(obj));
	}



}
