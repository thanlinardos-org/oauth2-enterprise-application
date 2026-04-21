package com.thanlinardos.resource_server.repository;

import com.thanlinardos.resource_server.repository.api.CustomKeycloakEventRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** IMPORTANT: In order for jpa to pickup custom implementations, they need to follow this exact format as shown in this class: <interface name extending Jpa- or CrudRepository>Impl */
@RequiredArgsConstructor
@Repository
public class KeycloakEventRepositoryImpl implements CustomKeycloakEventRepository {

    private final EntityManager entityManager;

    @Override
    @Transactional
    public void updateEventToProcessed(long id) {
        entityManager.createQuery("update KeycloakEventJpa e set e.status='PROCESSED' where e.id=:id")
                .setParameter("id", id)
                .executeUpdate();
    }
}
