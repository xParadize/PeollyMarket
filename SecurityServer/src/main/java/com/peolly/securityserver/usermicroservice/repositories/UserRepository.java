package com.peolly.securityserver.usermicroservice.repositories;

import com.peolly.securityserver.usermicroservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findById(UUID userId);

    @Modifying
    @Query(value = "UPDATE user_roles SET role = 'ROLE_VERIFIED_EMAIL' WHERE user_id = ?1 AND role = 'ROLE_UNVERIFIED_EMAIL'", nativeQuery = true)
    void getVerificatedRole(UUID userId);
}
