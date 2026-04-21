package com.thanlinardos.resource_server.repository.api;

import com.thanlinardos.resource_server.model.entity.owner.ClientJpa;
import com.thanlinardos.resource_server.repository.base.BasicIdJpaRepository;

import java.util.Optional;

public interface ClientRepository extends BasicIdJpaRepository<ClientJpa> {

    Optional<ClientJpa> getFirstByName(String name);
}
