package com.thanlinardos.resource_server.model.mapped.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thanlinardos.resource_server.model.entity.base.BasicAuditableJpa;
import com.thanlinardos.spring_enterprise_library.model.entity.base.BasicIdJpa;
import com.thanlinardos.spring_enterprise_library.model.mapped.base.BasicIdModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@SuperBuilder
public abstract class BasicAuditableModel<T extends BasicIdJpa> extends BasicIdModel<T, BasicAuditableModel<T>> {

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private LocalDateTime createdAt;
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private String createdBy;
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private LocalDateTime updatedAt;
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private String updatedBy;

    protected BasicAuditableModel() {
    }

    protected BasicAuditableModel(BasicAuditableJpa entity) {
        super(entity);
        setCreatedAt(entity.getCreatedAt());
        setCreatedBy(entity.getCreatedBy());
        setUpdatedAt(entity.getUpdatedAt());
        setUpdatedBy(entity.getUpdatedBy());
    }
}
