package com.connorng.ReUzit.dto;

import com.connorng.ReUzit.model.Image;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingDTO {
    private Long id;
    private Long userId;
    private String username;
    private String title;
    private String description;
    private Long price;
    private Long categoryId;
    private String categoryName;
    private String condition;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private List<Image> images;
}
