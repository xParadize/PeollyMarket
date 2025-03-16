package com.peolly.catalogservice.services;

import com.peolly.catalogservice.exceptions.CategoryNotFoundException;
import com.peolly.catalogservice.models.Category;
import com.peolly.catalogservice.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category Not Found."));
    }
}
