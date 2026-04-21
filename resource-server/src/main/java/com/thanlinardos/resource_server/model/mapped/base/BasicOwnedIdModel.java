package com.thanlinardos.resource_server.model.mapped.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thanlinardos.resource_server.model.OwnedResource;
import com.thanlinardos.resource_server.model.entity.base.BasicOwnedEntity;
import com.thanlinardos.resource_server.model.mapped.OwnerModel;
import com.thanlinardos.spring_enterprise_library.model.entity.base.BasicIdJpa;
import com.thanlinardos.spring_enterprise_library.model.mapped.base.BasicIdModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public abstract class BasicOwnedIdModel<T extends BasicIdJpa> extends BasicIdModel<T, BasicOwnedIdModel<T>> implements OwnedResource<BasicOwnedIdModel<T>> {

    @ToString.Exclude
    @JsonIgnore
    private OwnerModel owner;

    protected BasicOwnedIdModel() {
        super();
    }

    protected BasicOwnedIdModel(BasicOwnedEntity entity) {
        super((BasicIdJpa) entity);
        this.setOwner(new OwnerModel(entity.getOwner()));
    }
}
