package com.thanlinardos.resource_server.service.role;

import com.thanlinardos.resource_server.aspect.annotation.ExcludeFromLoggingAspect;
import com.thanlinardos.resource_server.model.entity.role.AuthorityJpa;
import com.thanlinardos.resource_server.model.entity.role.RoleJpa;
import com.thanlinardos.resource_server.model.mapped.AuthorityModel;
import com.thanlinardos.resource_server.model.mapped.RoleModel;
import com.thanlinardos.resource_server.repository.api.AuthorityRepository;
import com.thanlinardos.resource_server.repository.api.RoleRepository;
import com.thanlinardos.spring_enterprise_library.model.entity.base.BasicIdJpa;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.base.Authority;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleCacheService {

    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "roles")
    public Collection<RoleModel> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleModel::new)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "roles", key = "#name")
    @ExcludeFromLoggingAspect
    @Nullable
    public RoleModel getRoleByName(String name) {
        return roleRepository.findByRole(name)
                .map(RoleModel::new)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "authorities")
    public List<Authority> getAllAuthorities() {
        return authorityRepository.findAll().stream()
                .map(AuthorityModel::new)
                .map(Authority.class::cast)
                .toList();
    }

    @Transactional
    @CachePut(value = "authorities", key = "#authority.name")
    public Authority addAuthority(AuthorityModel authority) {
        AuthorityJpa entity = AuthorityJpa.fromModel(authority);
        authorityRepository.findFirstByName(authority.getName())
                .map(BasicIdJpa::getId)
                .ifPresent(entity::setId);
        entity = authorityRepository.save(entity);
        authority.setId(entity.getId());
        return authority;
    }

    @Transactional
    @CachePut(value = "roles", key = "#role")
    public RoleModel linkAuthorityToRole(Long id, String role) {
        RoleJpa roleJpa = roleRepository.findByRole(role)
                .orElseThrow(() -> new IllegalArgumentException("Role not found from name: " + role));
        roleJpa.addAuthorityWithLink(AuthorityJpa.builder().id(id).build());
        roleRepository.save(roleJpa);
        return new RoleModel(roleJpa);
    }
}
