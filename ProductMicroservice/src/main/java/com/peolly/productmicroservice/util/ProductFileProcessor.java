package com.peolly.productmicroservice.util;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.peolly.productmicroservice.models.ProductCsvRepresentation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ProductFileProcessor {
    /**
     * Parses a CSV file containing product data using multithreading for performance optimization.
     *
     * @param file the CSV file containing product data.
     * @return a list of parsed product representations.
     */
    public List<ProductCsvRepresentation> parseCsv(MultipartFile file) {
        List<ProductCsvRepresentation> productCsvRepresentations = Collections.synchronizedList(new ArrayList<>());

        int threadsCount = 10;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> allLines = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                allLines.add(line);
            }

            int totalLines = allLines.size();
            int linesPerThread = totalLines / threadsCount;

            ExecutorService executorService = Executors.newFixedThreadPool(threadsCount);

            for (int i = 0; i < threadsCount; i++) {
                int start = i * linesPerThread;
                int end = (i == threadsCount - 1) ? totalLines : start + linesPerThread;

                List<String> subList = allLines.subList(start, end);

                executorService.submit(() -> {
                    List<ProductCsvRepresentation> products = getProductCsvRepresentations(subList);
                    productCsvRepresentations.addAll(products);
                });
            }
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return productCsvRepresentations;
    }

    /**
     * Converts a list of CSV lines into product representations using a CSV parsing strategy.
     *
     * @param subList the subset of CSV lines to process.
     * @return a list of parsed ProductCsvRepresentation objects.
     */
    private static List<ProductCsvRepresentation> getProductCsvRepresentations(List<String> subList) {
        String csvContent = String.join("\n", subList);
        StringReader stringReader = new StringReader(csvContent);

        ColumnPositionMappingStrategy<ProductCsvRepresentation> strategy = new ColumnPositionMappingStrategy<>();
        strategy.setType(ProductCsvRepresentation.class);
        strategy.setColumnMapping("name", "description", "companyId", "price");

        CsvToBean<ProductCsvRepresentation> csvToBean = new CsvToBeanBuilder<ProductCsvRepresentation>(stringReader)
                .withMappingStrategy(strategy)
                .withIgnoreEmptyLine(true)
                .withIgnoreLeadingWhiteSpace(true)
                .build();
        return csvToBean.parse();
    }
}
