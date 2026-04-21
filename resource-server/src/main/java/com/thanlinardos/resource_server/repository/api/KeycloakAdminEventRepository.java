package com.thanlinardos.resource_server.repository.api;

import com.thanlinardos.resource_server.batch.keycloak.event.EventStatusType;
import com.thanlinardos.resource_server.model.entity.keycloak.KeycloakAdminEventJpa;
import com.thanlinardos.resource_server.repository.base.BasicIdJpaRepository;

import java.util.Collection;
import java.util.List;

public interface KeycloakAdminEventRepository extends BasicIdJpaRepository<KeycloakAdminEventJpa>, CustomKeycloakAdminEventRepository {

    List<KeycloakAdminEventJpa> findAllByStatusIn(Collection<EventStatusType> statuses);
}
