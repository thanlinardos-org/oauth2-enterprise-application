package com.thanlinardos.resource_server.model.entity.role;

import com.thanlinardos.resource_server.model.entity.owner.OwnerJpa;
import com.thanlinardos.spring_enterprise_library.model.entity.base.BasicIdJpa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@SuperBuilder
public class RoleJpa extends BasicIdJpa {

    @Column(length = 50, nullable = false, unique = true)
    private String role;

    @Column(nullable = false, columnDefinition = "int default 2147483647")
    private int privilegeLvl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "owner_roles",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "owner_id"))
    @ToString.Exclude
    @Builder.Default
    private List<OwnerJpa> owners = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_authorities",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id"))
    @ToString.Exclude
    @Builder.Default
    private List<AuthorityJpa> authorities = new ArrayList<>();

    public void addAuthorityWithLink(AuthorityJpa authority) {
        authorities.add(authority);
        authority.getRoles().add(this);
    }
}
