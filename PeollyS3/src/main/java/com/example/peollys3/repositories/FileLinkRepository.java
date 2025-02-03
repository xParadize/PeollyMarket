package com.example.peollys3.repositories;

import com.example.peollys3.entities.FileLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileLinkRepository extends JpaRepository<FileLink, Long> {
}
