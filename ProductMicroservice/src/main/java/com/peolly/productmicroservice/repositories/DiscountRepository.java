package com.peolly.productmicroservice.repositories;

import com.peolly.productmicroservice.models.Discount;
import com.peolly.productmicroservice.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {
    void deleteAll();
    void deleteAllByProduct(Product product);
}
