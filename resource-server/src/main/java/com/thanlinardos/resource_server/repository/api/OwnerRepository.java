package com.thanlinardos.resource_server.repository.api;

import com.thanlinardos.resource_server.model.entity.owner.OwnerJpa;
import com.thanlinardos.resource_server.repository.base.BasicIdJpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OwnerRepository extends BasicIdJpaRepository<OwnerJpa>, CustomOwnerRepository {

    Optional<OwnerJpa> getFirstByName(String name);

    Optional<OwnerJpa> getFirstByClientServiceAccountId(UUID serviceAccountId);

    Optional<OwnerJpa> getFirstByCustomerUsername(String username);

    boolean existsByClientServiceAccountId(UUID serviceAccountId);

    boolean existsByUuid(UUID uuid);

    boolean existsByName(String name);

    Optional<OwnerJpa> getFirstByUuid(UUID uuid);
}
