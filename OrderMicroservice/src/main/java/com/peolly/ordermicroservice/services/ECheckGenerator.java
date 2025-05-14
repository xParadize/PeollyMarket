package com.peolly.ordermicroservice.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.peolly.ordermicroservice.dto.CartDto;
import com.peolly.ordermicroservice.external.ItemDto;
import com.peolly.ordermicroservice.models.OrderElement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ECheckGenerator {
    public byte[] generateECheck(UUID userId, CartDto cartData) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        document.setMargins(0, 0, 5, 0);

        Paragraph header = new Paragraph(new Phrase("Cash receipt No.123\n" +
                getECheckDateAndTime(LocalDateTime.now()) + "\n" +
                "https://www.peollymarket.com\n" +
                "INN 1234567890\n" +
                "Type of taxation: OSN\n" +
                "Income\n"));
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);

        int orderNumber = 1;
        BigDecimal totalPrice = BigDecimal.valueOf(0);

        for (OrderElement element : cartData.getCart().getOrderElements()) {
            ItemDto item = element.getItemDto();
            int quantity = element.getQuantity();

            BigDecimal totalPriceForCurrentItems = BigDecimal.valueOf(item.price()).multiply(BigDecimal.valueOf(quantity));
            totalPrice = totalPrice.add(totalPriceForCurrentItems);

            // Разделитель
            Paragraph separator = new Paragraph(new Phrase("------------------------------"));
            separator.setAlignment(Element.ALIGN_CENTER);
            document.add(separator);

            // Основная информация о товаре
            PdfPTable productTable = new PdfPTable(3);
            productTable.setWidthPercentage(100);
            productTable.setWidths(new float[]{10, 70, 20});

            productTable.addCell(new PdfPCell(new Phrase(orderNumber + ".")));
            productTable.addCell(new PdfPCell(new Phrase(item.name())));
            productTable.addCell(new PdfPCell(new Phrase(String.format("= %.2f", totalPriceForCurrentItems))));

            productTable.addCell(new PdfPCell(new Phrase("")));
            productTable.addCell(new PdfPCell(new Phrase(String.format("%d x %.2f", quantity, item.price()))));
            productTable.addCell(new PdfPCell(new Phrase("")));

            productTable.addCell(new PdfPCell(new Phrase("")));
            productTable.addCell(new PdfPCell(new Phrase("Without NDS")));
            productTable.addCell(new PdfPCell(new Phrase(String.format("= %.2f", totalPriceForCurrentItems))));

            document.add(productTable);
            orderNumber++;
        }

        // Итоговая стоимость
        PdfPTable totalCostTable = new PdfPTable(2);
        totalCostTable.setWidthPercentage(100);
        totalCostTable.setWidths(new float[]{70, 30});

        totalCostTable.addCell(new PdfPCell(new Phrase("Total cost")));
        totalCostTable.addCell(new PdfPCell(new Phrase(String.format("= %.2f", totalPrice))));

        totalCostTable.addCell(new PdfPCell(new Phrase("Without NDS")));
        totalCostTable.addCell(new PdfPCell(new Phrase(String.format("= %.2f", totalPrice))));

        totalCostTable.addCell(new PdfPCell(new Phrase("Prepayment")));
        totalCostTable.addCell(new PdfPCell(new Phrase("")));

        totalCostTable.addCell(new PdfPCell(new Phrase("Non-cash")));
        totalCostTable.addCell(new PdfPCell(new Phrase(String.format("= %.2f", totalPrice))));

        document.add(totalCostTable);

        document.close();
        return outputStream.toByteArray();
    }

    public String getECheckName() {
        String packageOfSymbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        StringBuilder fileName = new StringBuilder();
        String fileExtension = ".pdf";

        for (int i = 0; i < 30; i++) {
            int index = (int) (packageOfSymbols.length() * Math.random());
            fileName.append(packageOfSymbols.charAt(index));
        }

        return fileName + fileExtension;
    }

    private String getECheckDateAndTime(LocalDateTime timeNow) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return timeNow.format(formatter);
    }
}
