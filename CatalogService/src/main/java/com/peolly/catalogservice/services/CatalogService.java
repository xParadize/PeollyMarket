package com.peolly.catalogservice.services;

import com.peolly.catalogservice.repositories.CatalogRepository;
import com.peolly.catalogservice.client.StorageServiceClient;
import com.peolly.catalogservice.dto.CreateProductRequest;
import com.peolly.catalogservice.dto.ItemDuplicateRequest;
import com.peolly.catalogservice.kafka.CatalogKafkaProducer;
import com.peolly.catalogservice.mappers.CatalogItemMapper;
import com.peolly.catalogservice.models.CatalogItem;
import com.peolly.catalogservice.models.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final StorageServiceClient storageServiceClient;
    private final CatalogRepository catalogRepository;
    private final CatalogItemMapper catalogItemMapper;
    private final CategoryService categoryService;
    private final CatalogKafkaProducer catalogKafkaProducer;

    @Transactional
    public boolean isProductDuplicate(CreateProductRequest createProductRequest) {
        ItemDuplicateRequest validationRequest = new ItemDuplicateRequest(
                createProductRequest.getName(),
                createProductRequest.getDescription()
        );
        return storageServiceClient.isDuplicate(validationRequest);
    }

    @Transactional
    public void saveCatalogItem(CreateProductRequest createProductRequest, String creatorEmail) {
        Category category = categoryService.findCategoryById(createProductRequest.getCategoryId());

        CatalogItem catalogItem = catalogItemMapper.toEntity(createProductRequest);
        catalogItem.setCategory(category);

        catalogRepository.save(catalogItem);

        catalogKafkaProducer.sendCreateItemEvent(createProductRequest, creatorEmail, catalogItem.getId());

        //TODO: оповещаем цены + стораж + емеил о новом товаре
    }
}
