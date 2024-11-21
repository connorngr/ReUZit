package com.connorng.ReUzit.controller.listing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListingRequest {
    private String title;
    private String description;
    private Double price;
    private String condition;
    private String status;
    private List<MultipartFile> images;
    private Long userId;  // ID of the user creating the listing
    private Long categoryId;  // ID of the category

}
