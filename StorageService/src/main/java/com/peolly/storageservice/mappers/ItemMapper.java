package com.peolly.storageservice.mappers;

import com.peolly.schemaregistry.CreateItemEvent;
import com.peolly.storageservice.models.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemMapper {
//    @Mapping(target = "categoryId", ignore = true)
//    @Mapping(target = "price", ignore = true)
//    @Mapping(target = "discount", ignore = true)
//    @Mapping(target = "email", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    Item toEntity(CreateItemEvent createItemEvent);

    default String map(CharSequence value) {
        return value == null ? null : value.toString();
    }
}
