package com.thanlinardos.resource_server.repository.api;

import com.thanlinardos.resource_server.model.entity.account.AccountTransactionJpa;
import com.thanlinardos.resource_server.repository.base.BasicIdJpaRepository;

public interface AccountTransactionRepository extends BasicIdJpaRepository<AccountTransactionJpa>, CustomAccountTransactionRepository {
}
