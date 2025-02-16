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
    /**
     * Validates a list of product CSV representations and returns validation summaries
     * containing any errors or duplicate issues.
     *
     * @param products the list of product representations from CSV.
     * @return a list of validation summaries.
     */
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

    /**
     * Validates a single product CSV representation and returns a list of validation error reports.
     *
     * @param productCsvRepresentation the product representation to validate.
     * @return a list of validation error reports.
     */
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

    /**
     * Checks if a product CSV representation has duplicate fields based on existing products in the database.
     *
     * @param productCsvRepresentation the product representation to check for duplicates.
     * @return a list of duplicate error reports if duplicates are found.
     */
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

    /**
     * Creates a duplicate report based on existing product names and descriptions.
     *
     * @param productRepresentation the product representation to check.
     * @param existingNames         the set of existing product names.
     * @param existingDescriptions  the set of existing product descriptions.
     * @return a ProductsDuplicateReport indicating whether the product is a duplicate.
     */
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

    /**
     * Constructs an error message indicating which fields are duplicated.
     *
     * @param productRepresentation the product representation being checked.
     * @param isNameDuplicate       whether the name is a duplicate.
     * @param isDescriptionDuplicate whether the description is a duplicate.
     * @return a formatted duplicate error message.
     */
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

