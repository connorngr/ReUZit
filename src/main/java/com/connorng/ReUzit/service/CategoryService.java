package com.connorng.ReUzit.service;

import com.connorng.ReUzit.model.Category;
import com.connorng.ReUzit.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public Optional<Category> findById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

}
