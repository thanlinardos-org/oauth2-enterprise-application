package com.thanlinardos.resource_server.repository.api;

import com.thanlinardos.resource_server.model.entity.role.AuthorityJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorityRepository extends JpaRepository<AuthorityJpa, Long> {

    Optional<AuthorityJpa> findFirstByName(String name);
}
