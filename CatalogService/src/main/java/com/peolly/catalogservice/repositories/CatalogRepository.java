package com.peolly.catalogservice.repositories;

import com.peolly.catalogservice.models.CatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatalogRepository extends JpaRepository<CatalogItem, Long> {

}
