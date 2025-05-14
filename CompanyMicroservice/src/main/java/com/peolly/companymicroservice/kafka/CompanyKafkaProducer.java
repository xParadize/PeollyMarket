package com.peolly.companymicroservice.kafka;

import com.peolly.companymicroservice.models.ProductCsvRepresentation;
import com.peolly.companymicroservice.repositories.ProductCsvRepresentationMapping;
import com.peolly.schemaregistry.CreateProductEvent;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CompanyKafkaProducer {
    private final KafkaTemplate<String, GenericRecord> kafkaTemplate;
    private final ProductCsvRepresentationMapping productCsvRepresentationMapping;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public void sendCreateProduct(List<ProductCsvRepresentation> csvRepresentationList) {
        List<ProducerRecord<String, GenericRecord>> records = csvRepresentationList.stream()
                .map(this::convertCsvRepresentationToRecord)
                .toList();

        records.forEach(kafkaTemplate::send);
        LOGGER.info("Batch of {} messages written to topic '{}'", records.size(), "create-product-requests");
    }

    private ProducerRecord<String, GenericRecord> convertCsvRepresentationToRecord(ProductCsvRepresentation p) {
        CreateProductEvent event = convertCsvRepresentationToEvent(p);
        return new ProducerRecord<>(
                "create-product-requests",
                "Company Microservice",
                event
        );
    }

    private CreateProductEvent convertCsvRepresentationToEvent(ProductCsvRepresentation csvRepresentation) {
        return productCsvRepresentationMapping.toEvent(csvRepresentation);
    }
}
