package com.peolly.companymicroservice.repositories;

import com.peolly.companymicroservice.models.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findCompanyById(Long id);
    Optional<Company> findByName(String name);
    Optional<Company> findByCreatorId(UUID userId);
    Page<Company> findAll(Pageable var1);
}
