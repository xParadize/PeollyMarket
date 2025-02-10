package com.peolly.productmicroservice.controllers;

import com.peolly.productmicroservice.dto.ApiResponse;
import com.peolly.productmicroservice.dto.CreateProductRequest;
import com.peolly.productmicroservice.dto.ProductDto;
import com.peolly.productmicroservice.dto.ProductMapper;
import com.peolly.productmicroservice.exceptions.IncorrectSearchPath;
import com.peolly.productmicroservice.models.Product;
import com.peolly.productmicroservice.services.ProductService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/store")
@Tag(name = "Store main page")
public class ProductController {
    @Value("${PRODUCTS_PER_PAGE}")
    private Integer productsPerPage;

    @Value("${ORGANIZATIONS_PER_PAGE}")
    private Integer organizationsPerPage;

    private final ProductService productsService;
    private final ProductMapper productMapper;

    @Hidden
    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

//    @Operation(summary = "Get all products in the store")
//    @GetMapping()
//    public List<ProductDto> getAllProducts(@RequestParam(value = "page", defaultValue = "0", required = false) int page,
//                                           @RequestParam(value = "companyId", required = false) Long companyId) throws ExecutionException, InterruptedException {
//        if (companyId != null) {
//            return productsService.findProductsByCompanyIdWithPagination(companyId, page, productsPerPage);
//        } else {
//            return productsService.productsPagination(page, productsPerPage);
//        }
//    }
//
    @Operation(summary = "Shows one product by its id")
    @GetMapping("/product/{id}")
    public ResponseEntity<?> showProductInfo(@PathVariable("id") Long id) {
        Product product = productsService.findProductById(id);
        if (product == null) {
            return new ResponseEntity<>(new ApiResponse(false, "Product not found"), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(convertProductToDto(product), HttpStatus.OK);
    }

//    @GetMapping("/companies")
//    public List<OrganizationMainPageInfo> showAllCompanies(@RequestParam(value = "page", defaultValue = "0", required = false) int page) {
//        return organizationService.showAllOrganizations(page, organizationsPerPage).getContent();
//    }

    @Operation(summary = "Create product")
    @PostMapping(value = "/create-product")
    public ResponseEntity<ApiResponse> createProduct(@RequestBody @Valid CreateProductRequest createProductRequest, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            String errors = getFieldsErrors(bindingResult);
            return new ResponseEntity<>(new ApiResponse(false, errors), HttpStatus.BAD_REQUEST);
        }

        boolean valid = productsService.isProductValid(createProductRequest);
        if (!valid) {
            return new ResponseEntity<>(new ApiResponse(false, "Product contains duplicate data. Please fix it and make request again."), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new ApiResponse(true, "Product added. You will receive an email notification with more information."), HttpStatus.OK);
    }

    @Operation(summary = "Create products")
    @PostMapping(value = "/create-products", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse> createProducts(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "email") String email) {

        if (file == null && email == null) {
            throw new IllegalArgumentException("Please provide a file and email for report.");
        }

        if (file == null || email == null) {
            throw new IllegalArgumentException("Please provide a CSV file and email for report.");
        }

        productsService.validateProductsInFile(file, email);
        return new ResponseEntity<>(new ApiResponse(true, "Products sent to validation."), HttpStatus.OK);
    }

    private String getFieldsErrors(BindingResult bindingResult) {
        String errorMessage = bindingResult.getFieldErrors()
                .stream()
                .map(error -> String.format("%s - %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return errorMessage;
    }

    private ProductDto convertProductToDto(Product product) {
        return productMapper.toDto(product);
    }
}