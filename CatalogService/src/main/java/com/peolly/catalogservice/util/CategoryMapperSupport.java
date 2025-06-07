package com.peolly.catalogservice.util;

import com.peolly.catalogservice.models.Category;
import com.peolly.catalogservice.repositories.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CategoryMapperSupport {
    private CategoryRepository categoryRepository;

    public Category map(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category with id %d not found".formatted(id)));
    }
}