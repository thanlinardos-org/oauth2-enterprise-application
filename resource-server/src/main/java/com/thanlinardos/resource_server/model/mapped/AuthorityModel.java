package com.thanlinardos.resource_server.model.mapped;

import com.thanlinardos.resource_server.model.entity.role.AuthorityJpa;
import com.thanlinardos.resource_server.model.info.AuthorityInfo;
import com.thanlinardos.spring_enterprise_library.model.mapped.base.BasicIdModel;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.base.Authority;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.types.AccessType;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@AllArgsConstructor
@SuperBuilder
@ToString
@Getter
public class AuthorityModel extends BasicIdModel<AuthorityJpa, AuthorityModel> implements Authority {

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

    public static AuthorityModel fromAuthorityInfo(AuthorityInfo authorityInfo) {
        return AuthorityModel.builder() //NOSONAR (S3252)
                .name(authorityInfo.name())
                .access(authorityInfo.access())
                .uri(authorityInfo.uri())
                .expression(authorityInfo.expression())
                .build();
    }

    @Override
    public AuthorityJpa toEntity() {
        return AuthorityJpa.builder() //NOSONAR (S3252)
                .name(getName())
                .accessType(getAccess())
                .expression(getExpression())
                .uri(getUri())
                .build();
    }

    @Override
    public AuthorityJpa toEntityOnlyId() {
        return AuthorityJpa.builder() //NOSONAR (S3252)
                .id(getId())
                .build();
    }

    @Override
    public AuthorityModel fromEntity(AuthorityJpa entity) {
        return new AuthorityModel(entity);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AuthorityModel that = (AuthorityModel) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }
}
