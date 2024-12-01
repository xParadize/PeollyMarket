package com.peolly.productmicroservice.controllers;

import com.peolly.paymentmicroservice.exceptions.IncorrectSearchPath;
import com.peolly.productmicroservice.services.ProductService;
import com.peolly.utilservice.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

    @Hidden
    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

//    @Operation(summary = "Get all products in the store")
//    @GetMapping()
//    public List<ProductDTO> getAllProducts(@RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
//                                           @RequestParam(value = "company", required = false) String organizationName) {
//        if (organizationName != null) {
//            return productsService.findProductsByOrganizationNameWithPagination(organizationName, page, productsPerPage);
//        } else {
//            return productsService.productsPagination(page, productsPerPage);
//        }
//    }
//
//    @Operation(summary = "Shows one product by its id")
//    @GetMapping("/product/{id}")
//    public ResponseEntity<?> showProductInfo(@PathVariable("id") Long productId) {
//        ProductDTO showProductInfo = productsService.getProductInfo(productId);
//        if (showProductInfo == null) {
//            return new ResponseEntity<>(new ApiResponse(false, "Item with this ID not found"), HttpStatus.NOT_FOUND);
//        }
//        return new ResponseEntity<>(showProductInfo, HttpStatus.OK);
//    }
//
//    @GetMapping("/companies")
//    public List<OrganizationMainPageInfo> showAllCompanies(@RequestParam(value = "page", defaultValue = "0", required = false) int page) {
//        return organizationService.showAllOrganizations(page, organizationsPerPage).getContent();
//    }

    private String getFieldsErrors(BindingResult bindingResult) {
        String errorMessage = bindingResult.getFieldErrors()
                .stream()
                .map(error -> String.format("%s - %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return errorMessage;
    }
}