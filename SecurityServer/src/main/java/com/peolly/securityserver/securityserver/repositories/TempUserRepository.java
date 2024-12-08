package com.peolly.securityserver.securityserver.repositories;

import com.peolly.securityserver.securityserver.models.TemporaryUser;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempUserRepository extends KeyValueRepository<TemporaryUser, String> {
}
