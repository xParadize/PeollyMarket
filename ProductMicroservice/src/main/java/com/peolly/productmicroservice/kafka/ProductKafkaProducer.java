package com.peolly.productmicroservice.kafka;

import com.peolly.utilservice.events.GetCompanyByIdEvent;
import com.peolly.utilservice.events.ProductDataHaveProblemsEvent;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class ProductKafkaProducer {
    private final KafkaTemplate<String, ProductDataHaveProblemsEvent> sendIsProductDescriptionRepeatEvent;
    private final KafkaTemplate<String, GetCompanyByIdEvent> sendGetCompanyByIdEvent;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public void sendEmailConfirmed(List<String> invalidFields) {
        ProductDataHaveProblemsEvent event = new ProductDataHaveProblemsEvent(invalidFields);
        ProducerRecord<String, ProductDataHaveProblemsEvent> record = new ProducerRecord<>(
                "send-product-duplicate-detected",
                event
        );
        sendIsProductDescriptionRepeatEvent.send(record);
        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
    }

    public void sendGetCompanyById(Long companyId) {
        GetCompanyByIdEvent event = new GetCompanyByIdEvent(companyId);
        ProducerRecord<String, GetCompanyByIdEvent> record = new ProducerRecord<>(
                "send-get-company-by-id",
                event
        );
        sendGetCompanyByIdEvent.send(record);
        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
    }
}
