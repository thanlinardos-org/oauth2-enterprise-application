package com.thanlinardos.resource_server.repository.api;

import com.thanlinardos.resource_server.batch.keycloak.event.EventStatusType;
import com.thanlinardos.resource_server.model.entity.keycloak.KeycloakEventJpa;
import com.thanlinardos.resource_server.repository.base.BasicIdJpaRepository;

import java.util.Collection;
import java.util.List;

public interface KeycloakEventRepository extends BasicIdJpaRepository<KeycloakEventJpa>, CustomKeycloakEventRepository {

    List<KeycloakEventJpa> findAllByStatusIn(Collection<EventStatusType> statuses);
}
