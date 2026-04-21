package com.thanlinardos.resource_server.repository.api;

import com.thanlinardos.resource_server.model.entity.owner.CustomerJpa;
import com.thanlinardos.resource_server.repository.base.BasicIdJpaRepository;

import java.util.Optional;

public interface CustomerRepository extends BasicIdJpaRepository<CustomerJpa> {

    Optional<CustomerJpa> findByEmail(String email);
}
