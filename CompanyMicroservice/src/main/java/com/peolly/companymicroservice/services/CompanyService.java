package com.peolly.companymicroservice.services;


import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.peolly.companymicroservice.kafka.CompanyKafkaProducer;
import com.peolly.companymicroservice.kafka.KafkaProducerConfiguration;
import com.peolly.companymicroservice.models.Company;
import com.peolly.companymicroservice.models.ProductCsvRepresentation;
import com.peolly.companymicroservice.repositories.CompanyRepository;
import jakarta.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyKafkaProducer companyKafkaProducer;

//    @Transactional(readOnly = true)
//    public Optional<Company> getCompanyById(Long id) {
//        return companyRepository.findCompanyById(id);
//    }

    @Transactional(readOnly = true)
    public void checkErrorsInFile(MultipartFile file) throws IOException {
        List<ProductCsvRepresentation> products = parseCsv(file);
        List<String> validationErrors = getErrorFields(products);
        if (validationErrors.isEmpty()) {
            uploadProducts(products);
        } else {
            companyKafkaProducer.sendCreateProductValidationErrors(validationErrors);
        }

    }

    private List<ProductCsvRepresentation> parseCsv(MultipartFile file) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            HeaderColumnNameMappingStrategy<ProductCsvRepresentation> strategy =
                    new HeaderColumnNameMappingStrategy<>();
            strategy.setType(ProductCsvRepresentation.class);

            CsvToBean<ProductCsvRepresentation> csvToBean =
                    new CsvToBeanBuilder<ProductCsvRepresentation>(reader)
                            .withMappingStrategy(strategy)
                            .withIgnoreEmptyLine(true)
                            .withIgnoreLeadingWhiteSpace(true)
                            .build();

            return csvToBean.parse()
                    .stream()
                    .map(csvLine -> ProductCsvRepresentation.builder()
                            .name(csvLine.getName())
                            .description(csvLine.getDescription())
                            .companyId(csvLine.getCompanyId())
                            .price(csvLine.getPrice())
                            .build()
                    )
                    .collect(Collectors.toList());
        }
    }

    private void uploadProducts(List<ProductCsvRepresentation> products) {
        int threadsCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadsCount);
        int batchSize = products.size() / threadsCount;

        for (int i = 0; i < threadsCount; i++) {
            int start = i * batchSize;
            int end = (i + 1) * batchSize;
            List<ProductCsvRepresentation> subList = products.subList(start, end);
            executorService.submit(() -> companyKafkaProducer.sendCreateProduct(subList));
        }
    }

    private List<String> getErrorFields(List<@Valid ProductCsvRepresentation> products) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        List<String> errors = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            ProductCsvRepresentation product = products.get(i);
            Set<ConstraintViolation<ProductCsvRepresentation>> violations = validator.validate(product);
            if (!violations.isEmpty()) {
                for (ConstraintViolation<ProductCsvRepresentation> violation : violations) {
                    errors.add(String.format("Row %s: %s", i + 1, violation.getMessage()));
                }
            }
        }
        return errors;
    }

//    @Transactional
//    @KafkaListener(topics = "send-get-company-by-id", groupId = "org-deli-queuing-product")
//    public void consumeGetCompanyByIdEvent(GetCompanyByIdEvent event) {
//        Optional<Company> company = getCompanyById(event.companyId());
//        companyKafkaProducer.sendGetCompanyByIdResponse(company, event.companyId());
//    }
//
//    @Transactional(readOnly = true)
//    public Organization findOrganizationByName(String name) {
//        return organizationRepository.findByName(name).
//                orElse(null);
//    }
//
//    @Transactional(readOnly = true)
//    public Page<OrganizationMainPageInfo> showAllOrganizations(int page, int organizationsPerPage) {
//        Page<Organization> organizations = organizationRepository.findAll(PageRequest.of(page, organizationsPerPage));
//        List<OrganizationMainPageInfo> orgInfoList = new ArrayList<>();
//        for (Organization o : organizations) {
//            orgInfoList.add(convertOrgToOrgMainPageInfo(o));
//        }
//        return new PageImpl<>(orgInfoList);
//    }
//
//    @Transactional(readOnly = true)
//    public boolean isUserAlreadyHasOrganization(UUID userId) {
//        Optional<Organization> organization = organizationRepository.findByCreatorId(userId);
//        return organization.isPresent();
//    }
//
//    @Transactional
//    public void save(Organization organization) {
//        organizationRepository.save(organization);
//    }
//
//    private OrganizationMainPageInfo convertOrgToOrgMainPageInfo(Organization organization) {
//        OrganizationMainPageInfo orgPageInfo = OrganizationMainPageInfo.builder()
//                .organizationName(organization.getName())
//                .link(String.format("http://%s:%s/store?company=%s", hostName, hostPort, organization.getName()))
//                .build();
//        return orgPageInfo;
//    }
}

