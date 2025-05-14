package com.peollys3.repositories;

import com.peollys3.entities.FileLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileLinkRepository extends JpaRepository<FileLink, Long> {
}
