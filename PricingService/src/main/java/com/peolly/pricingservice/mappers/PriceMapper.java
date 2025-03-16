package com.peolly.pricingservice.mappers;

import com.peolly.pricingservice.models.Price;
import com.peolly.schemaregistry.CreateItemEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface PriceMapper {
    @Mapping(target = "itemId", source = "id")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    Price toEntity(CreateItemEvent createItemEvent);

    default String map(CharSequence value) {
        return value == null ? null : value.toString();
    }
}