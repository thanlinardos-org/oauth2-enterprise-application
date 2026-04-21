package com.thanlinardos.resource_server.model.mapped;

import com.thanlinardos.resource_server.model.entity.role.RoleJpa;
import com.thanlinardos.resource_server.model.info.RoleInfo;
import com.thanlinardos.spring_enterprise_library.model.mapped.base.BasicIdModel;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.base.Authority;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.base.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.thanlinardos.spring_enterprise_library.spring_cloud_security.constants.SecurityCommonConstants.ROLE_PREFIX;

@AllArgsConstructor
@SuperBuilder
@Getter
public class RoleModel extends BasicIdModel<RoleJpa, RoleModel> implements Serializable, Role {

    private String role;
    private int privilegeLvl;
    @Builder.Default
    private List<AuthorityModel> authorities = new ArrayList<>();

    public RoleModel(RoleJpa entity) {
        super(entity);
        this.role = entity.getRole();
        this.privilegeLvl = entity.getPrivilegeLvl();
        this.authorities = entity.getAuthorities().stream()
                .map(AuthorityModel::new)
                .toList();
    }

    public static RoleModel fromRoleInfo(RoleInfo roleInfo) {
        return RoleModel.builder() //NOSONAR (S3252)
                .role(roleInfo.name())
                .privilegeLvl(roleInfo.privilegeLvl())
                .build();
    }

    @Override
    public Collection<GrantedAuthority> getGrantedAuthorities() {
        return authorities.stream()
                .map(Authority::getName)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    @Override
    public String getName() {
        return role;
    }

    public String getNameNoPrefix() {
        return role.startsWith(ROLE_PREFIX) ? role.substring(ROLE_PREFIX.length()) : role;
    }

    @Override
    public RoleJpa toEntity() {
        return RoleJpa.builder() //NOSONAR (S3252)
                .id(getId())
                .role(getRole())
                .privilegeLvl(getPrivilegeLvl())
                .authorities(new ArrayList<>(getAuthorities().stream()
                        .map(AuthorityModel::toEntity)
                        .toList()))
                .build();
    }

    @Override
    public RoleJpa toEntityOnlyId() {
        return RoleJpa.builder() //NOSONAR (S3252)
                .id(getId())
                .build();
    }

    @Override
    public RoleModel fromEntity(RoleJpa entity) {
        return new RoleModel(entity);
    }

    @Override
    public String toString() {
        return "Role[name=" + role + ", lvl=" + privilegeLvl + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RoleModel roleModel = (RoleModel) o;
        return role.equals(roleModel.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), role);
    }
}
