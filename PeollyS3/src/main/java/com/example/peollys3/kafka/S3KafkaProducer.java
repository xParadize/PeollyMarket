package com.example.peollys3.kafka;

import com.peolly.schemaregistry.ProductValidationErrorsEvent;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3KafkaProducer {
    private final KafkaTemplate<String, GenericRecord> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public void sendFileLinkToEmail(String uploadLink, String email) {
        ProductValidationErrorsEvent event = ProductValidationErrorsEvent.newBuilder()
                .setUploadLink(uploadLink)
                .setReceiverEmail(email)
                .build();
        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(
                "product-validation-errors",
                "Peolly S3",
                event
        );
        kafkaTemplate.send(record);
        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
    }
}
