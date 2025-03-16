package com.peolly.companymicroservice.repositories;

import com.peolly.companymicroservice.models.ProductCsvRepresentation;
import com.peolly.schemaregistry.CreateProductEvent;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductCsvRepresentationMapping {
    CreateProductEvent toEvent(ProductCsvRepresentation csvRepresentation);

    default String map(CharSequence value) {
        return value == null ? null : value.toString();
    }
}