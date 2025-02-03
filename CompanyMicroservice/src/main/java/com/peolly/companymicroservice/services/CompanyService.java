package com.peolly.companymicroservice.services;


import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.peolly.companymicroservice.kafka.CompanyKafkaProducer;
import com.peolly.companymicroservice.models.ProductCsvRepresentation;
import com.peolly.companymicroservice.models.ProductValidationReport;
import jakarta.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
    public void checkErrorsInFile(MultipartFile file, String email) throws Exception {
        List<ProductCsvRepresentation> products = parseCsv(file);
        List<ProductValidationReport> validationErrorReports = getErrorFields(products);
        if (validationErrorReports.isEmpty()) {
            uploadProducts(products);
        } else {
            createCsvWithValidationErrors(validationErrorReports, email);
        }

    }

    private void createCsvWithValidationErrors(List<ProductValidationReport> reports, String email) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8))) {
            for (ProductValidationReport report : reports) {
                for (String errorMessage : report.getErrorMessages()) {
                    writer.printf("Row %d: %s - %s%n",
                            report.getRow(),
                            report.getIncorrectData(),
                            errorMessage);
                }
            }
        }
        sendCsvToReceiver(byteArrayOutputStream.toByteArray(), email);
    }

    private List<ProductCsvRepresentation> parseCsv(MultipartFile file) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            ColumnPositionMappingStrategy<ProductCsvRepresentation> strategy =
                    new ColumnPositionMappingStrategy<>();
            strategy.setType(ProductCsvRepresentation.class);

            String[] columns = {"name", "description", "companyId", "price"};
            strategy.setColumnMapping(columns);

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
        int batchSize = (int) Math.ceil((double) products.size() / threadsCount);
        for (int i = 0; i < threadsCount; i++) {
            int start = i * batchSize;
            int end = Math.min((i + 1) * batchSize, products.size());
            List<ProductCsvRepresentation> subList = products.subList(start, end);
            executorService.submit(() -> companyKafkaProducer.sendCreateProduct(subList));
        }
    }

    private List<ProductValidationReport> getErrorFields(List<@Valid ProductCsvRepresentation> products) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        List<ProductValidationReport> reports = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            ProductCsvRepresentation product = products.get(i);
            Set<ConstraintViolation<ProductCsvRepresentation>> violations = validator.validate(product);
            if (!violations.isEmpty()) {
                List<String> errors = new ArrayList<>();
                for (ConstraintViolation<ProductCsvRepresentation> violation : violations) {
                    errors.add(violation.getMessage());
                }
                ProductValidationReport report = ProductValidationReport.builder()
                        .row(i + 1)
                        .isProductValid(false)
                        .errorMessages(errors)
                        .incorrectData(product.toCsvString())
                        .build();
                reports.add(report);
            }
        }
        return reports;
    }

    private void sendCsvToReceiver(byte[] csvData, String email) {
        RestTemplate restTemplate = new RestTemplate();

        ByteArrayResource byteArrayResource = new ByteArrayResource(csvData) {
            @Override
            public String getFilename() {
                long time = System.currentTimeMillis();
                return time + "_validation_errors.csv";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", byteArrayResource);
        body.add("email", email);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        restTemplate.postForEntity("http://localhost:8010/s3/upload", requestEntity, String.class);
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

