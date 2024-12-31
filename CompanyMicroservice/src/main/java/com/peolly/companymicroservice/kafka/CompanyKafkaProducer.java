package com.peolly.companymicroservice.kafka;

import com.peolly.companymicroservice.dto.CreateProductDto;
import com.peolly.companymicroservice.models.Company;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class CompanyKafkaProducer {
//    private final KafkaTemplate<String, CreateProductEvent> sendCreateProductEvent;
//    private final KafkaTemplate<String, GetCompanyByIdResponseEvent> getCompanyByIdResponseEvent;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

//    public void sendCreateProduct(CreateProductDto createProductDto) {
//        var futureProduct = new CreateProductEvent(
//                createProductDto.getName(),
//                createProductDto.getDescription(),
//                createProductDto.getCompanyId(),
//                createProductDto.getPrice()
//        );
//        ProducerRecord<String, CreateProductEvent> record = new ProducerRecord<>(
//                "send-create-product",
//                futureProduct
//        );
//        sendCreateProductEvent.send(record);
//        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
//    }

//    public void sendGetCompanyByIdResponse(Optional<Company> optionalCompany, Long companyId) {
//        GetCompanyByIdResponseEvent event = optionalCompany
//                .map(company -> new GetCompanyByIdResponseEvent(companyId,true, company.getName()))
//                .orElse(new GetCompanyByIdResponseEvent(companyId, false, null));
//        ProducerRecord<String, GetCompanyByIdResponseEvent> record = new ProducerRecord<>(
//                "send-get-company-by-id-response",
//                event
//        );
//        getCompanyByIdResponseEvent.send(record);
//        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
//    }
}
