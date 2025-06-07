package com.peolly.catalogservice.mappers;

import com.peolly.catalogservice.dto.CreateProductRequest;
import com.peolly.catalogservice.dto.ItemCsvRepresentation;
import com.peolly.catalogservice.models.CatalogItem;
import com.peolly.catalogservice.util.CategoryMapperSupport;
import com.peolly.schemaregistry.CreateItemEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = CategoryMapperSupport.class)
public interface CatalogItemMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    CatalogItem toEntity(CreateProductRequest createProductRequest);

    @Mapping(target = "id", source = "itemId")
    @Mapping(target = "email", source = "email")
    CreateItemEvent toEvent(CreateProductRequest createProductRequest, String email, Long itemId);

    @Mapping(target = "id", source = "itemId")
    @Mapping(target = "email", source = "email")
    CreateItemEvent toEvent(ItemCsvRepresentation csv, String email, Long itemId);

    CatalogItem toDto(ItemCsvRepresentation productCsvRepresentation);

    default String map(CharSequence value) {
        return value == null ? null : value.toString();
    }
}