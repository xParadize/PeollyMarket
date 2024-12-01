package com.peolly.companymicroservice.controllers;

import com.peolly.companymicroservice.dto.CreateProductDto;
import com.peolly.companymicroservice.exceptions.CompanyNotFoundException;
import com.peolly.companymicroservice.exceptions.IncorrectSearchPath;
import com.peolly.companymicroservice.kafka.CompanyKafkaListenerFutureWaiter;
import com.peolly.companymicroservice.services.CompanyService;
import com.peolly.utilservice.ApiResponse;
import com.peolly.utilservice.events.SendCreateProductEvent;
import com.peolly.utilservice.events.SendProductDataHaveProblemsEvent;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
@Tag(name = "Company")
public class CompanyController {

    private final CompanyService companyService;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final CompanyKafkaListenerFutureWaiter companyKafkaListenerFutureWaiter;
    private final KafkaTemplate<String, SendCreateProductEvent> sendCreateProductEvent;

    @Hidden
    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @Operation(summary = "Create product")
    @PostMapping("/create-product")
    public ResponseEntity<?> createProduct(@RequestBody @Valid CreateProductDto createProductDto, BindingResult bindingResult) throws ExecutionException, InterruptedException {
        if (bindingResult.hasFieldErrors()) {
            String errors = getFieldsErrors(bindingResult);
            return ResponseEntity.badRequest().body(new ApiResponse(false, errors));
        }

        boolean doesCompanyExist = companyService.getCompanyById(createProductDto.getCompanyId()).isPresent();
        if (!doesCompanyExist) {
            throw new CompanyNotFoundException();
        }

        var futureProduct = new SendCreateProductEvent(
                createProductDto.getName(),
                createProductDto.getDescription(),
                createProductDto.getCompanyId(),
                createProductDto.getPrice()
        );

        ProducerRecord<String, SendCreateProductEvent> record = new ProducerRecord<>(
                "send-create-product",
                futureProduct
        );

        SendResult<String, SendCreateProductEvent> result = sendCreateProductEvent
                .send(record).get();

        LOGGER.info("Sent event: {}", result);

        CompletableFuture<List<String>> future = new CompletableFuture<>();
        companyKafkaListenerFutureWaiter.setInvalidProductFields(future);

        List<String> invalidProductFields = future.get();
        if (invalidProductFields.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse(true, "Product created."), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse(
                false, String.format("Not unique product fields: %s", invalidProductFields)), HttpStatus.BAD_REQUEST);
    }

    @KafkaListener(topics = "send-product-duplicate-detected", groupId = "org-deli-queuing-product")
    public void consumeSendProductDuplicateDetected(SendProductDataHaveProblemsEvent problemsEvent) {
        CompletableFuture<List<String>> future = companyKafkaListenerFutureWaiter.getInvalidProductFields();
        future.complete(problemsEvent.invalidFields());
    }

//    @PostMapping("/add")
//    @Operation(summary = "Create company ticket")
//    public ResponseEntity<?> createTicket(@RequestBody OrganizationTicketDto organizationTicketDto, Principal actualUser) {
//        User companyCreator = usersService.findByUsername(actualUser.getName());
//        UUID companyCreatorId = companyCreator.getId();
//        organizationTicketService.createTicket(organizationTicketDto, companyCreatorId);
//        return new ResponseEntity<>(new ApiResponse(true, "Ticket has been created"), HttpStatus.OK);
//    }
//
//    @GetMapping("/my-ticket")
//    public ResponseEntity<?> viewUserTicket(Principal actualUser) {
//        User companyCreator = usersService.findByUsername(actualUser.getName());
//        UUID userId = companyCreator.getId();
//        OrganizationTicketDto ticketInformation = organizationTicketService.showUserTicket(userId);
//        return new ResponseEntity<>(ticketInformation, HttpStatus.OK);
//    }

    private String getFieldsErrors(BindingResult bindingResult) {
        String errorMessage = bindingResult.getFieldErrors()
                .stream()
                .map(error -> String.format("%s - %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return errorMessage;
    }
}
