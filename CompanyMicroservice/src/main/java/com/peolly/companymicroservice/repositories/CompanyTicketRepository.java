package com.peolly.companymicroservice.repositories;

import com.peolly.companymicroservice.models.CompanyTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyTicketRepository extends JpaRepository<CompanyTicket, Integer> {
    Page<CompanyTicket> findAll(Pageable var1);
    Optional<CompanyTicket> findByCreatorId(UUID userId);
}
