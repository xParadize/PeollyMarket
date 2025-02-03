package com.peolly.companymicroservice.controllers;

import com.peolly.companymicroservice.dto.ApiResponse;
import com.peolly.companymicroservice.exceptions.IncorrectSearchPath;
import com.peolly.companymicroservice.services.CompanyService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
@Tag(name = "Company")
public class CompanyController {
    private final CompanyService companyService;

    @Hidden
    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @Operation(summary = "Create product")
    @PostMapping(value = "/create-product", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse> createProduct(@RequestPart("file")MultipartFile file,
                                                     @RequestParam("email") String email) throws Exception {
        companyService.checkErrorsInFile(file, email);
        return new ResponseEntity<>(new ApiResponse(true, "Products sent to validation."), HttpStatus.OK);
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
}
