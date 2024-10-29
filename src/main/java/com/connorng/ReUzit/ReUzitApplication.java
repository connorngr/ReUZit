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
//			testS3Buckets(s3Service, s3Buckets);
//			createSampleListing();
		};
	}

	private static void testS3Buckets(S3Service s3Service, S3Buckets s3Buckets) {
		s3Service.putObject(s3Buckets.getListing(), "foo", "Hello world".getBytes());

		byte[] obj = s3Service.getObject(s3Buckets.getListing(), "foo");

		System.out.println("hooray: " + new String(obj));
	}

//	private MultipartFile createMultipartFileFromUrl(String imageUrl) throws IOException {
//		// Đọc nội dung hình ảnh từ URL
//		InputStream inputStream = new URL(imageUrl).openStream();
//		byte[] imageContent = inputStream.readAllBytes();
//
//		// Tạo MultipartFile từ nội dung hình ảnh
//		return new CustomMultipartFile(
//				"image",
//				"sample-image.jpg", // Bạn có thể thay đổi tên file tùy ý
//				"image/jpeg", // Thay đổi content type nếu hình ảnh là khác
//				imageContent
//		);
//	}

	private void createSampleListing() {
		ListingRequest listingRequest = new ListingRequest();
		listingRequest.setTitle("Sample Listing");
		listingRequest.setDescription("This is a sample description for a listing.");
		listingRequest.setPrice(100.0);
		listingRequest.setCondition("new");
		listingRequest.setStatus("active");
		listingRequest.setUserId(1L);  // Replace with a valid user ID
		listingRequest.setCategoryId(1L);  // Replace with a valid category ID

//		try {
			// Nếu muốn sử dụng hình ảnh từ URL
			String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/b/ba/Flower_jtca001.jpg";

			// Tạo MultipartFile từ URL
//			MultipartFile imageFromUrl = createMultipartFileFromUrl(imageUrl);

			// Tạo listing và thêm hình ảnh vào đó
//			Listing createdListing = listingService.createListing(
//					listingRequest,
//					"long15@gmail.com", // Email hợp lệ
//					Collections.singletonList(imageFromUrl) // Sử dụng danh sách chứa MultipartFile
//			);

			// Xuất thông tin listing đã tạo
//			System.out.println("Created Listing: " + createdListing);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

}
