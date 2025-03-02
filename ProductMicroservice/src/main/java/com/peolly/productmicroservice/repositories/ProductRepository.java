package com.peolly.productmicroservice.repositories;

import com.peolly.productmicroservice.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
        Page<Product> findAll(Pageable var1);
        Optional<Product> findProductByName(String name);
        Optional<Product> findProductByDescription(String description);
        Optional<Product> findProductById(Long id);
        int countProductsByCompanyId(Long companyId);

        @Query("SELECT p.name FROM Product p")
        Set<String> findAllProductNames();

        @Query("SELECT p.description FROM Product p")
        Set<String> findAllProductDescriptions();

        @Query(value = "SELECT * FROM Product WHERE company_id = :company_id ORDER BY company_id OFFSET :offset LIMIT :limit", nativeQuery = true)
        List<Product> findProductsByCompanyIdWithPagination(@Param("company_id") Long companyId, @Param("offset") int offset, @Param("limit") int limit);

        @Query(value = "SELECT * FROM product WHERE discount is not null", nativeQuery = true)
        List<Product> findAllProductsWithDiscount();

        @Modifying
        @Query(value = "UPDATE product SET discount = :discount_id WHERE id = :product_id", nativeQuery = true)
        void changeDiscount(@Param("discount_id") Integer discountId, @Param("product_id") Long productId);

        @Modifying
        @Query(value = "UPDATE product SET storage_amount = storage_amount - :amount WHERE id = :product_id", nativeQuery = true)
        void decreaseProductStorageAmount(@Param("product_id") Long productId, @Param("amount") int amount);
}
