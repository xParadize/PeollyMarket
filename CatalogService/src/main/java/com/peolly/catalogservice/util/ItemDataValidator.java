package com.peolly.catalogservice.util;

import com.peolly.catalogservice.client.StorageServiceClient;
import com.peolly.catalogservice.dto.*;
import jakarta.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ItemDataValidator {
    private final StorageServiceClient storageServiceClient;
    /**
     * Validates a list of item CSV representations and returns validation summaries
     * containing any errors or duplicate issues.
     *
     * @param items the list of item representations from CSV.
     * @return a list of validation summaries.
     */
    public List<ItemValidationSummary> getItemsValidationResult(List<@Valid ItemCsvRepresentation> items) {
        List<ItemDuplicateReport> duplicateReports = getAllDuplicateReports(items);

        List<ItemValidationSummary> summaries = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            ItemCsvRepresentation item = items.get(i);
            List<ItemValidationReport> validationErrors = getErrorFields(item);
            ItemDuplicateReport duplicateReport = duplicateReports.get(i);

            List<ItemDuplicateReport> duplicates = duplicateReport.isItemDuplicate() ?
                    List.of(duplicateReport) : Collections.emptyList();

            summaries.add(ItemValidationSummary.builder()
                    .validationErrors(validationErrors)
                    .duplicateErrors(duplicates)
                    .build());
        }
        return summaries;
    }


    /**
     * Validates a single item CSV representation and returns a list of validation error reports.
     *
     * @param ItemCsvRepresentation the item representation to validate.
     * @return a list of validation error reports.
     */
    private List<ItemValidationReport> getErrorFields(@Valid ItemCsvRepresentation ItemCsvRepresentation) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        List<ItemValidationReport> reports = new ArrayList<>();
        Set<ConstraintViolation<ItemCsvRepresentation>> violations = validator.validate(ItemCsvRepresentation);
        if (!violations.isEmpty()) {
            List<String> errors = new ArrayList<>();
            for (ConstraintViolation<ItemCsvRepresentation> violation : violations) {
                errors.add(violation.getMessage());
            }
            ItemValidationReport report = ItemValidationReport.builder()
                    .isItemValid(false)
                    .errorMessages(errors)
                    .incorrectData(ItemCsvRepresentation.toCsvString())
                    .build();
            reports.add(report);
        }
        return reports;
    }

    private List<ItemDuplicateReport> getAllDuplicateReports(List<ItemCsvRepresentation> items) {
        List<ItemDuplicateRequest> requests = items.stream()
                .map(p -> new ItemDuplicateRequest(p.getName(), p.getDescription()))
                .toList();

        List<Boolean> duplicateFlags = storageServiceClient.isDuplicate(requests);
        List<ItemDuplicateReport> reports = new ArrayList<>();

        for (int i = 0; i < duplicateFlags.size(); i++) {
            boolean isDuplicate = Boolean.TRUE.equals(duplicateFlags.get(i));
            ItemCsvRepresentation p = items.get(i);

            if (isDuplicate) {
                reports.add(ItemDuplicateReport.builder()
                        .isItemDuplicate(true)
                        .duplicateData(String.format("name '%s', description '%s'", p.getName(), p.getDescription()))
                        .errorMessages("already exist(s)")
                        .build());
            } else {
                reports.add(ItemDuplicateReport.builder()
                        .isItemDuplicate(false)
                        .duplicateData("")
                        .errorMessages("")
                        .build());
            }
        }

        return reports;
    }
}

