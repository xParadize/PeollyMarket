package com.peolly.securityserver.securityserver.tempregistration;

import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempUserRepository extends KeyValueRepository<TemporaryUser, String> {
}
