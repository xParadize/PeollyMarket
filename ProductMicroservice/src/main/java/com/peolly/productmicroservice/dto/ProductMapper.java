package com.peolly.productmicroservice.dto;

import com.peolly.productmicroservice.models.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {
    Product toEntity(ProductDto productDto);
    ProductDto toDto(Product product);
//    ProductDto toDto(SavePaymentMethodEvent event);
}
