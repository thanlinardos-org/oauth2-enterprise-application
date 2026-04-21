package com.thanlinardos.resource_server.repository.base;

import com.thanlinardos.spring_enterprise_library.model.entity.base.BasicIdJpa;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface BasicIdJpaExtendedRepository<T extends BasicIdJpa> {

    void saveOrUpdateFoundByProperty(T entity, Supplier<Optional<T>> findEntityBy);

    <S extends BasicIdJpa> void removeRelationWithId(T entity, Function<T, Collection<S>> getRelations, S subEntityWithId);
}
