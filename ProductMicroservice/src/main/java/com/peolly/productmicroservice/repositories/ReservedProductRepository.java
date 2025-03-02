package com.peolly.productmicroservice.repositories;

import com.peolly.productmicroservice.models.ReservedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservedProductRepository extends JpaRepository<ReservedProduct, Long> {
}
