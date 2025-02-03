package com.peolly.productmicroservice.util;

import com.peolly.productmicroservice.models.ProductCsvRepresentation;
import com.peolly.productmicroservice.models.ProductValidationReport;
import com.peolly.productmicroservice.models.ProductValidationSummary;
import com.peolly.productmicroservice.models.ProductsDuplicateReport;
import com.peolly.productmicroservice.repositories.ProductRepository;
import jakarta.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductDataValidator {
    private final ProductRepository productRepository;

    public List<ProductValidationSummary> getProductsValidationResult(List<@Valid ProductCsvRepresentation> products) {
        List<ProductValidationSummary> validationSummaries = new ArrayList<>();
        for (ProductCsvRepresentation p : products) {
            List<ProductValidationReport> validationErrors = getErrorFields(p);
            List<ProductsDuplicateReport> duplicateErrors = getProductsDuplicateFields(p);
            ProductValidationSummary summary =  ProductValidationSummary.builder()
                    .validationErrors(validationErrors)
                    .duplicateErrors(duplicateErrors)
                    .build();
            validationSummaries.add(summary);
        }

        return validationSummaries;
    }

    private List<ProductValidationReport> getErrorFields(@Valid ProductCsvRepresentation productCsvRepresentation) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        List<ProductValidationReport> reports = new ArrayList<>();
        Set<ConstraintViolation<ProductCsvRepresentation>> violations = validator.validate(productCsvRepresentation);
        if (!violations.isEmpty()) {
            List<String> errors = new ArrayList<>();
            for (ConstraintViolation<ProductCsvRepresentation> violation : violations) {
                errors.add(violation.getMessage());
            }
            ProductValidationReport report = ProductValidationReport.builder()
                    .isProductValid(false)
                    .errorMessages(errors)
                    .incorrectData(productCsvRepresentation.toCsvString())
                    .build();
            reports.add(report);
        }
        return reports;
    }

    private List<ProductsDuplicateReport> getProductsDuplicateFields(@Valid ProductCsvRepresentation productCsvRepresentation) {
        Set<String> existingNames = productRepository.findAllProductNames();
        Set<String> existingDescriptions = productRepository.findAllProductDescriptions();

        ProductsDuplicateReport duplicateReport = createDuplicateReport(
                productCsvRepresentation,
                existingNames,
                existingDescriptions
        );

        if (duplicateReport.isProductDuplicate()) {
            return List.of(duplicateReport);
        } else {
            return Collections.emptyList();
        }
    }

    private ProductsDuplicateReport createDuplicateReport(ProductCsvRepresentation productRepresentation, Set<String> existingNames, Set<String> existingDescriptions) {
        boolean isNameDuplicate = existingNames.contains(productRepresentation.getName());
        boolean isDescriptionDuplicate = existingDescriptions.contains(productRepresentation.getDescription());

        String duplicateData = makeDuplicateErrorMessage(productRepresentation, isNameDuplicate, isDescriptionDuplicate);

        return ProductsDuplicateReport.builder()
                .isProductDuplicate(isNameDuplicate || isDescriptionDuplicate)
                .duplicateData(duplicateData)
                .errorMessages(isNameDuplicate || isDescriptionDuplicate ? "already exist(s)" : "")
                .build();
    }

    private static String makeDuplicateErrorMessage(ProductCsvRepresentation productRepresentation, boolean isNameDuplicate, boolean isDescriptionDuplicate) {
        String duplicateData = "";
        if (isNameDuplicate && isDescriptionDuplicate) {
            duplicateData = String.format("name '%s', description '%s'", productRepresentation.getName(), productRepresentation.getDescription());
        } else if (isNameDuplicate) {
            duplicateData = String.format("name '%s'", productRepresentation.getName());
        } else if (isDescriptionDuplicate) {
            duplicateData = String.format("description '%s'", productRepresentation.getDescription());
        }
        return duplicateData;
    }
}

