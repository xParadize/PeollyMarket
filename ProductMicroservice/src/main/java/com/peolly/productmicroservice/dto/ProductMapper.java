package com.peolly.productmicroservice.dto;

import com.peolly.productmicroservice.models.Product;
import com.peolly.productmicroservice.models.ProductCsvRepresentation;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {
    ProductDto toDto(CreateProductRequest createProductRequest);
    ProductDto toDto(UpdateProductRequest updateProductRequest);
    ProductDto toDto(Product product);
    Product toDto(ProductCsvRepresentation productCsvRepresentation);
    Product toEntity(ProductDto productDto);
    void updateProductData(UpdateProductRequest updateProductRequest, @MappingTarget Product product);
}
