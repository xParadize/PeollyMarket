package com.peolly.productmicroservice.controllers;

import com.peolly.productmicroservice.dto.ApiResponse;
import com.peolly.productmicroservice.dto.ProductDto;
import com.peolly.productmicroservice.dto.ProductMapper;
import com.peolly.productmicroservice.exceptions.IncorrectSearchPath;
import com.peolly.productmicroservice.models.Product;
import com.peolly.productmicroservice.services.ProductService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            return new ResponseEntity<>(new ApiResponse(false, "Item with this ID not found"), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(convertProductToDto(product), HttpStatus.OK);
    }
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

    private ProductDto convertProductToDto(Product product) {
        return productMapper.toDto(product);
    }
}