package com.thanlinardos.resource_server.repository.api;

import com.thanlinardos.resource_server.model.entity.economy.LoanJpa;
import com.thanlinardos.resource_server.repository.base.BasicIdJpaRepository;

import java.util.List;

public interface LoanRepository extends BasicIdJpaRepository<LoanJpa> {

    List<LoanJpa> getByOwnerNameOrderByStartDtDesc(String name);
}
