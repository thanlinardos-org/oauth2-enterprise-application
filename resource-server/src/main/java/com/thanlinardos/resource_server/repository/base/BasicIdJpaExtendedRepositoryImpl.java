package com.thanlinardos.resource_server.repository.base;

import com.thanlinardos.spring_enterprise_library.model.entity.base.BasicIdJpa;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class BasicIdJpaExtendedRepositoryImpl<T extends BasicIdJpa> extends SimpleJpaRepository<T, Long> implements BasicIdJpaExtendedRepository<T> {

    private final EntityManager entityManager;

    public BasicIdJpaExtendedRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    @Transactional
    @Override
    public void saveOrUpdateFoundByProperty(T entity, Supplier<Optional<T>> findEntityBy) {
        findEntityBy.get()
                .map(BasicIdJpa::getId)
                .ifPresent(entity::setId);
        entityManager.persist(entity);
        entityManager.refresh(entity);
    }

    @Transactional
    @Override
    public <S extends BasicIdJpa> void removeRelationWithId(T entity, Function<T, Collection<S>> getRelations, S subEntityWithId) {
        getRelations.apply(entity).remove(subEntityWithId);
        entityManager.persist(entity);
        entityManager.refresh(entity);
    }
}
