package com.peolly.productmicroservice.dto;

import com.peolly.productmicroservice.models.Product;
import com.peolly.productmicroservice.models.ProductCsvRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {
    ProductDto toDto(CreateProductRequest createProductRequest);
    ProductDto toDto(Product product);
    Product toDto(ProductCsvRepresentation productCsvRepresentation);
    Product toEntity(ProductDto productDto);
}
