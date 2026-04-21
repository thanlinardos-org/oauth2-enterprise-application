package com.thanlinardos.resource_server.repository.api;

import com.thanlinardos.resource_server.model.entity.role.RoleJpa;
import com.thanlinardos.resource_server.repository.base.BasicIdJpaRepository;

import java.util.Optional;

public interface RoleRepository extends BasicIdJpaRepository<RoleJpa> {

    Optional<RoleJpa> findByRole(String role);
}
