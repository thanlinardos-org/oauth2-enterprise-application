package com.thanlinardos.resource_server.model.mapped;

import com.thanlinardos.resource_server.model.entity.role.AuthorityJpa;
import com.thanlinardos.spring_enterprise_library.model.mapped.base.BasicIdModel;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.base.Authority;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.types.AccessType;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
@Getter
public class AuthorityModel extends BasicIdModel implements Authority {

    private String name;
    private AccessType access;
    private String uri;
    @Nullable
    private String expression;

    public AuthorityModel(AuthorityJpa entity) {
        super(entity);
        this.name = entity.getName();
        this.access = entity.getAccessType();
        this.uri = entity.getUri();
        this.expression = entity.getExpression();
    }
}
