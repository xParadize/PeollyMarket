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
import java.util.stream.Collectors;

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

    private CreateProductEvent convertCsvRepresentationToEvent(ProductCsvRepresentation csvRepresentation) {
        return productCsvRepresentationMapping.toEvent(csvRepresentation);
    }

    private List<CharSequence> convertListStringToListCharSequence(List<String> strings) {
        return strings.stream()
                .map(CharSequence.class::cast)
                .collect(Collectors.toList());
    }
}
