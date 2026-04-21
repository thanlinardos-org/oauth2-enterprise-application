package com.thanlinardos.resource_server.service;

import com.thanlinardos.resource_server.repository.base.BasicIdJpaRepository;
import com.thanlinardos.spring_enterprise_library.error.errorcodes.ErrorCode;
import com.thanlinardos.spring_enterprise_library.model.entity.base.BasicIdJpa;
import com.thanlinardos.spring_enterprise_library.model.mapped.base.BasicIdModel;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class ModelServiceHelper<T extends BasicIdJpa, M extends BasicIdModel<T, M>, S extends BasicIdJpa, N extends BasicIdModel<S, N>> {

    private final BasicIdJpaRepository<T> repository;
    private final BasicIdJpaRepository<S> subRepository;

    public M saveOrUpdateEntityFoundBy(M model, Supplier<Optional<T>> findEntityBy) {
        T entity = model.toEntity();
        repository.saveOrUpdateFoundByProperty(entity, findEntityBy);
        model.setId(entity.getId());
        return model;
    }

    public N saveOrUpdateSubEntityFoundBy(N model, Supplier<Optional<S>> findEntityBy) {
        S entity = model.toEntity();
        subRepository.saveOrUpdateFoundByProperty(entity, findEntityBy);
        model.setId(entity.getId());
        return model;
    }

    public M linkToModel(M model, Supplier<Optional<T>> findEntitySupplier, Function<T, Collection<S>> getRelations, S subEntityWithId) {
        T entity = findEntity(model, findEntitySupplier);
        getRelations.apply(entity).add(subEntityWithId);
        repository.save(entity);
        return model.fromEntity(entity);
    }

    public M unlinkFromModel(M model, Supplier<Optional<T>> findEntitySupplier, Function<T, Collection<S>> getRelations, S subEntityWithId) {
        T entity = findEntity(model, findEntitySupplier);
        repository.removeRelationWithId(entity, getRelations, subEntityWithId);
        return model.fromEntity(entity);
    }

    public M createWithLinks(M model, Collection<N> existingLinks, Set<N> newLinks, boolean unlinkOthers, Function<M, Optional<T>> findEntityBy, Function<T, Collection<S>> getRelations) {
        BiConsumer<M, N> linker = (m, n) -> linkToModel(m, () -> findEntityBy.apply(m), getRelations, n.toEntityOnlyId());
        BiConsumer<M, N> unlinker = (m, n) -> unlinkFromModel(m, () -> findEntityBy.apply(m), getRelations, n.toEntityOnlyId());
        return ModelUtils.createWithLinks(model, existingLinks, newLinks, unlinkOthers, linker, unlinker);
    }

    public N createSubWithLinks(N model, Collection<M> existingLinks, Set<M> newLinks, boolean unlinkOthers, Function<M, Optional<T>> findEntityBy, Function<T, Collection<S>> getRelations) {
        BiConsumer<N, M> linker = (n, m) -> linkToModel(m, () -> findEntityBy.apply(m), getRelations, n.toEntityOnlyId());
        BiConsumer<N, M> unlinker = (n, m) -> unlinkFromModel(m, () -> findEntityBy.apply(m), getRelations, n.toEntityOnlyId());
        return ModelUtils.createWithLinks(model, existingLinks, newLinks, unlinkOthers, linker, unlinker);
    }

    private T findEntity(M model, Supplier<Optional<T>> findEntitySupplier) {
        return findEntitySupplier.get()
                .orElseThrow(() -> ErrorCode.ILLEGAL_ARGUMENT.createCoreException("Entity of model {0} not found by method {1}", new Object[]{model.getClass().getSimpleName(), findEntitySupplier.toString()}));
    }
}
