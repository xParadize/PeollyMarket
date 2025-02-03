package com.peolly.productmicroservice.kafka;

import com.peolly.schemaregistry.ProductCreatedEvent;
import lombok.AllArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class ProductKafkaProducer {
    private final KafkaTemplate<String, GenericRecord> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public void sendProductCreated(String productName, String email) {
        ProductCreatedEvent event = new ProductCreatedEvent(productName, email);
        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(
                "product-created",
                event
        );
        kafkaTemplate.send(record);
        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
    }
}
