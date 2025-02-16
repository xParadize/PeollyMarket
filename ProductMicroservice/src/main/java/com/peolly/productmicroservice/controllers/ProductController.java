package com.peolly.productmicroservice.controllers;

import com.peolly.productmicroservice.dto.*;
import com.peolly.productmicroservice.exceptions.IncorrectSearchPath;
import com.peolly.productmicroservice.models.Product;
import com.peolly.productmicroservice.services.ProductService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/store")
@Tag(name = "Store main page")
public class ProductController {
    private final ProductService productsService;
    private final ProductMapper productMapper;

    @Hidden
    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    /**
     * Retrieves and displays product information by its ID.
     *
     * @param id the unique identifier of the product.
     * @return ResponseEntity containing the product information if found, otherwise an error message.
     */
    @Operation(summary = "Shows one product by its id")
    @GetMapping("/product/{id}")
    public ResponseEntity<?> showProductInfo(@PathVariable("id") Long id) {
        Optional<Product> productOpt = productsService.findProductById(id);
        if (productOpt.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse(false, "Product not found."), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(convertProductToDto(productOpt.get()), HttpStatus.OK);
    }

    /**
     * Creates a new product based on the provided request data.
     *
     * @param createProductRequest the request object containing product details.
     * @param bindingResult        holds validation results of the request body.
     * @return ResponseEntity<ApiResponse> indicating success or validation errors.
     */
    @Operation(summary = "Create product")
    @PostMapping(value = "/create-product")
    public ResponseEntity<ApiResponse> createProduct(@RequestBody @Valid CreateProductRequest createProductRequest, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            String errors = getFieldsErrors(bindingResult);
            return new ResponseEntity<>(new ApiResponse(false, errors), HttpStatus.BAD_REQUEST);
        }

        boolean valid = productsService.validateProduct(createProductRequest);
        if (!valid) {
            return new ResponseEntity<>(new ApiResponse(false, "Product contains duplicate data. Please fix it and make request again."), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new ApiResponse(true, "Product added. You will receive an email notification with more information."), HttpStatus.OK);
    }

    /**
     * Updates an existing product with the given ID using provided data.
     *
     * @param updateProductRequest the request object containing updated product details.
     * @param id                   the unique identifier of the product to update.
     * @param bindingResult        holds validation results of the request body.
     * @return ResponseEntity containing the updated product or an error message.
     */
    @PatchMapping(value = "/update-product/{id}")
    public ResponseEntity<?> updateProduct(@RequestBody @Valid UpdateProductRequest updateProductRequest,
                                                     @PathVariable Long id,
                                                     BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            String errors = getFieldsErrors(bindingResult);
            return new ResponseEntity<>(new ApiResponse(false, errors), HttpStatus.BAD_REQUEST);
        }

        Optional<Product> requestedProduct = productsService.findProductById(id);
        if (requestedProduct.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse(false, "Product not found."), HttpStatus.NOT_FOUND);
        }

        boolean valid = productsService.validateUpdatedProduct(id, updateProductRequest);
        if (!valid) {
            return new ResponseEntity<>(new ApiResponse(false, "Product contains duplicate data. Please fix it and make request again."), HttpStatus.BAD_REQUEST);
        }

        Product updatedProduct = productsService.updateProduct(id, updateProductRequest);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Processes a CSV file to create multiple products and sends a validation report via email.
     *
     * @param file  the CSV file containing product data.
     * @param email the email address to receive the validation report.
     * @return ResponseEntity<ApiResponse> indicating whether the products were sent for validation.
     */
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

        productsService.validateProducts(file, email);
        return new ResponseEntity<>(new ApiResponse(true, "Products sent to validation."), HttpStatus.OK);
    }

    /**
     * Collects and formats field validation errors from the BindingResult.
     *
     * @param bindingResult the object holding field validation errors.
     * @return a formatted string containing all field errors.
     */
    private String getFieldsErrors(BindingResult bindingResult) {
        String errorMessage = bindingResult.getFieldErrors()
                .stream()
                .map(error -> String.format("%s - %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return errorMessage;
    }

    /**
     * Converts a Product entity into a ProductDto object.
     *
     * @param product the Product entity to be converted.
     * @return the corresponding ProductDto object.
     */
    private ProductDto convertProductToDto(Product product) {
        return productMapper.toDto(product);
    }
}