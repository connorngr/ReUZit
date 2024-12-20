package com.connorng.ReUzit.controller.listing;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ListingUpdateRequest {
    private String title;
    private String description;
    private Long price;
    private String condition;
    private String status;
    private Long userId;  // ID of the user creating the listing
    private Long categoryId;  // ID of the category
}
