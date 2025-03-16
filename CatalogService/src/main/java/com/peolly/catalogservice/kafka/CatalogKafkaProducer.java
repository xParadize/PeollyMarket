package com.peolly.catalogservice.kafka;

import com.peolly.catalogservice.dto.CreateProductRequest;
import com.peolly.catalogservice.mappers.CatalogItemMapper;
import com.peolly.schemaregistry.CreateItemEvent;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CatalogKafkaProducer {
    private final KafkaTemplate<String, GenericRecord> kafkaTemplate;
    private final CatalogItemMapper catalogItemMapper;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public void sendCreateItemEvent(CreateProductRequest createProductRequest, String email, Long itemId) {
        CreateItemEvent event = catalogItemMapper.toEvent(createProductRequest, email, itemId);

        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(
                "item-created",
                event
        );
        kafkaTemplate.send(record);
        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
    }
}
