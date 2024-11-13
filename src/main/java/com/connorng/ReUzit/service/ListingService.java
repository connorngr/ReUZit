package com.connorng.ReUzit.service;

import com.connorng.ReUzit.controller.listing.ListingRequest;
import com.connorng.ReUzit.model.Category;
import com.connorng.ReUzit.model.Image;
import com.connorng.ReUzit.model.Listing;
import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.repository.ListingRepository;
import com.connorng.ReUzit.s3.S3Buckets;
import com.connorng.ReUzit.s3.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.connorng.ReUzit.Common.FileStorageService;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

//Add admin the permission later
@Service
public class ListingService {
    @Autowired
    private ListingRepository listingRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private S3Buckets s3Buckets;

    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }

    public Optional<Listing> getListingById(Long listingId, String userEmail) {
        Optional<User> userOptional = userService.findByEmail(userEmail);

        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }

        return listingRepository.findById(listingId);
    }

    //    public Listing createListing(ListingRequest listingRequest, String authenticatedEmail, List<MultipartFile> listingImageFiles) {
    public List<Listing> getListingsByUserEmail(String userEmail) {
        // Find the user by email
        Optional<User> userOptional = userService.findByEmail(userEmail);

        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }

        // Return listings of the user
        return listingRepository.findByUser(userOptional);
    }


    //    public Listing createListing(ListingRequest listingRequest, String authenticatedEmail, List<MultipartFile> listingImageFiles) {
    public Listing createListing(ListingRequest listing, String authenticatedEmail) throws IOException {
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }
        // Fetch the category by its ID
        Optional<Category> categoryOptional = categoryService.findById(listing.getCategoryId());
        if (!categoryOptional.isPresent()) {
            throw new IllegalArgumentException("Category not found.");
        }

        // Create the listing and associate the fetched user and category
        Listing new_listing = new Listing();
        new_listing.setTitle(listing.getTitle());
        new_listing.setDescription(listing.getDescription());
        new_listing.setPrice(listing.getPrice());
        new_listing.setCondition(listing.getCondition());
        new_listing.setStatus(listing.getStatus());
        new_listing.setUser(userOptional.get());  // Set the authenticated user
        new_listing.setCategory(categoryOptional.get());  // Set the associated category

        // Save the listing and return the response
        List<Image> images = new ArrayList<>();
        for (MultipartFile file : listing.getImages()) {
            String imageUrl = fileStorageService.saveFileToStorage(file);  // Implement your logic for saving the file
            Image image = new Image();
            image.setUrl(imageUrl);
            image.setListing(new_listing);
            images.add(image);
        }
        new_listing.setImages(images);
        return listingRepository.save(new_listing);
    }


    public Listing updateListing(Long listingId, ListingRequest listingRequest, String authenticatedEmail) throws IOException {
        // Fetch the existing listing from the database
        Optional<Listing> listingOptional = checkIfListingExists(listingId);
        Listing listing = listingOptional.orElseThrow(() -> new RuntimeException("Listing not found"));

        // Fetch the authenticated user
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }

        User authenticatedUser = userOptional.get();

        // Check if the authenticated user owns the listing
        boolean isAdmin = authenticatedUser.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        System.out.println("Admin have role is: " + isAdmin);
        if (!isAdmin && !listing.getUser().getId().equals(authenticatedUser.getId())) {
            throw new SecurityException("You are not authorized to update listing with ID " + listingId);
        }

        // Optionally update the category
        if (listingRequest.getCategoryId() != null) {
            Optional<Category> categoryOptional = categoryService.findById(listingRequest.getCategoryId());
            if (!categoryOptional.isPresent()) {
                throw new IllegalArgumentException("Category not found.");
            }
            listing.setCategory(categoryOptional.get());
        }

        // Update other fields based on the request
        if (listingRequest.getTitle() != null) {
            listing.setTitle(listingRequest.getTitle());
        }

        if (listingRequest.getDescription() != null) {
            listing.setDescription(listingRequest.getDescription());
        }

        if (listingRequest.getPrice() != null) {
            listing.setPrice(listingRequest.getPrice());
        }

        if (listingRequest.getCondition() != null) {
            listing.setCondition(listingRequest.getCondition());
        }

        if (listingRequest.getStatus() != null) {
            listing.setStatus(listingRequest.getStatus());
        }
        List<Image> images = new ArrayList<>();
        for (MultipartFile file : listingRequest.getImages()) {
            String imageUrl = fileStorageService.saveFileToStorage(file);  // Implement your logic for saving the file
            Image image = new Image();
            image.setUrl(imageUrl);
            image.setListing(listing);
            images.add(image);
        }
        listing.setImages(images);
        return listingRepository.save(listing);
    }

    public List<Long> deleteListings(List<Long> ids, String authenticatedEmail) {
        // Lấy thông tin người dùng hiện tại
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        User authenticatedUser = userOptional.orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Danh sách ID không thể xóa
        List<Long> failedDeletions = new ArrayList<>();

        // Xác định nếu người dùng là Admin
        boolean isAdmin = authenticatedUser.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        for (Long listingId : ids) {
            try {
                // Kiểm tra nếu `listing` tồn tại
                Optional<Listing> listingOptional = checkIfListingExists(listingId);
                Listing listing = listingOptional.orElseThrow(() -> new RuntimeException("Listing with ID " + listingId + " not found"));

                // Kiểm tra quyền sở hữu `listing`
                if (!isAdmin && !listing.getUser().getId().equals(authenticatedUser.getId())) {
                    throw new SecurityException("You are not authorized to delete listing with ID " + listingId);
                }

                // Xóa `listing` khỏi cơ sở dữ liệu
                listingRepository.deleteById(listingId);

            } catch (Exception e) {
                // Nếu có lỗi, thêm ID vào danh sách thất bại và in ra lỗi cho debug
                failedDeletions.add(listingId);
                System.out.println("Failed to delete listing with ID " + listingId + ": " + e.getMessage());
            }
        }
        return failedDeletions; // Trả về danh sách ID không xóa được, rỗng nếu xóa thành công toàn bộ
    }

    private Optional<Listing> checkIfListingExists(Long listingId) {
        Optional<Listing> listingOptional = listingRepository.findById(listingId);
        if (!listingOptional.isPresent()) {
            throw new IllegalArgumentException("Listing not found.");
        }
        return listingOptional;
    }

    public void uploadListingImage(List<MultipartFile> listingImageFiles, Long id) {
        Optional<Listing> listingOptional = checkIfListingExists(id); // Check if the listing exists
        Listing listing = listingOptional.orElseThrow(() -> new RuntimeException("Listing not found"));

        // Step 1: Remove old images from S3 and the database
        if (listing.getImages() != null && !listing.getImages().isEmpty()) {
            // Clear old images from the listing
            listing.getImages().clear();
        }

        // Step 2: Add new images
        for (MultipartFile file : listingImageFiles) {
            String listingImageId = UUID.randomUUID().toString(); // Generate a new image ID
            String fileName = "listing-images/%s/%s".formatted(id, listingImageId); // File path in S3

            try {
                // Upload the image to S3
                s3Service.putObject(
                        s3Buckets.getListing(),
                        fileName,
                        file.getBytes()
                );

                // Step 3: Create a new Image entity and set the URL as listingImageId (key)
                Image image = new Image();
                image.setListing(listing);
                image.setUrl(listingImageId); // Set the URL as the key (listingImageId)

                // Add new image to the listing
                listing.getImages().add(image);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image", e);
            }
        }

        // Step 4: Save the updated listing
        listingRepository.save(listing);
    }

    public List<byte[]> getListingImages(Long id) {
        // Step 1: Check if the listing exists
        Optional<Listing> listingOptional = checkIfListingExists(id);
        Listing listing = listingOptional.orElseThrow(() -> new RuntimeException("Listing not found"));

        // Step 2: Check if there are any images for the listing
        if (listing.getImages() == null || listing.getImages().isEmpty()) {
            throw new RuntimeException("No images found for this listing");
        }

        // Step 3: Initialize a list to store image data
        List<byte[]> listingImages = new ArrayList<>();

        // Step 4: Retrieve each image from S3 using the listingImageId (stored in Image.url)
        for (Image image : listing.getImages()) {
            String listingImageId = image.getUrl();  // This is where we store the S3 key (listingImageId)
            byte[] listingImage = s3Service.getObject(
                    s3Buckets.getListing(),
                    "listing-images/%s/%s".formatted(id, listingImageId) // Fetch image using listing id and image id
            );
            listingImages.add(listingImage);
        }

        // Step 5: Return the list of images (as byte arrays)
        return listingImages;
    }

}
