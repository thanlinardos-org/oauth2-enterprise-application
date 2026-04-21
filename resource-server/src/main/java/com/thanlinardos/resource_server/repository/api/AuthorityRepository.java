package com.thanlinardos.resource_server.repository.api;

import com.thanlinardos.resource_server.model.entity.role.AuthorityJpa;
import com.thanlinardos.resource_server.repository.base.BasicIdJpaRepository;

import java.util.Optional;

public interface AuthorityRepository extends BasicIdJpaRepository<AuthorityJpa> {

    Optional<AuthorityJpa> findFirstByName(String name);
}
