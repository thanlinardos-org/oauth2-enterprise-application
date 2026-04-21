package com.thanlinardos.resource_server.repository.api;

import com.thanlinardos.resource_server.model.entity.account.AccountJpa;
import com.thanlinardos.resource_server.repository.base.BasicIdJpaRepository;

import java.util.Optional;

public interface AccountRepository extends BasicIdJpaRepository<AccountJpa> {

    Optional<AccountJpa> findByAccountNumber(long accountNumber);

    Optional<AccountJpa> findByOwnerName(String ownerName);
}
