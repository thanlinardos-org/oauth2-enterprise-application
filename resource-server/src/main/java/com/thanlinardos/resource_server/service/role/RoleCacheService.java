package com.thanlinardos.resource_server.service.role;

import com.thanlinardos.resource_server.aspect.annotation.ExcludeFromLoggingAspect;
import com.thanlinardos.resource_server.model.entity.role.AuthorityJpa;
import com.thanlinardos.resource_server.model.entity.role.RoleJpa;
import com.thanlinardos.resource_server.model.mapped.AuthorityModel;
import com.thanlinardos.resource_server.model.mapped.RoleModel;
import com.thanlinardos.resource_server.repository.api.AuthorityRepository;
import com.thanlinardos.resource_server.repository.api.RoleRepository;
import com.thanlinardos.resource_server.service.ModelServiceHelper;
import com.thanlinardos.spring_enterprise_library.objects.utils.CollectionUtils;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.base.Authority;
import jakarta.annotation.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class RoleCacheService {

    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;
    private final ModelServiceHelper<RoleJpa, RoleModel, AuthorityJpa, AuthorityModel> roleAuthServiceHelper;
    private final CacheManager cacheManager;

    public RoleCacheService(RoleRepository roleRepository, AuthorityRepository authorityRepository, CacheManager cacheManager) {
        this.roleRepository = roleRepository;
        this.authorityRepository = authorityRepository;
        this.cacheManager = cacheManager;
        this.roleAuthServiceHelper = new ModelServiceHelper<>(roleRepository, authorityRepository);
    }

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
    public AuthorityModel saveAuthority(AuthorityModel authority) {
        return roleAuthServiceHelper.saveOrUpdateSubEntityFoundBy(authority, () -> authorityRepository.findFirstByName(authority.getName()));
    }

    @Transactional
    @CachePut(value = "roles", key = "#role.name")
    public RoleModel linkAuthorityToRole(AuthorityModel authority, RoleModel role) {
        return roleAuthServiceHelper.linkToModel(role, () -> findByRoleName(role), RoleJpa::getAuthorities, authority.toEntityOnlyId());
    }

    @Transactional
    @CachePut(value = "roles", key = "#role.name")
    public RoleModel unlinkAuthorityFromRole(AuthorityModel authority, RoleModel role) {
        return roleAuthServiceHelper.unlinkFromModel(role, () -> findByRoleName(role), RoleJpa::getAuthorities, authority.toEntityOnlyId());
    }

    @Transactional
    @CachePut(value = "roles", key = "#role.name")
    public RoleModel createRoleWithAuthorities(RoleModel role, Set<AuthorityModel> newAuthorities, boolean unlinkFromOtherAuthorities) {
        return roleAuthServiceHelper.createWithLinks(
                role,
                role.getAuthorities(),
                newAuthorities,
                unlinkFromOtherAuthorities,
                m -> roleRepository.findByRole(m.getName()),
                RoleJpa::getAuthorities
        );
    }

    @Transactional
    @CachePut(value = "authorities", key = "#model.name")
    public AuthorityModel createAuthorityWithRoles(AuthorityModel model, Set<RoleModel> existingRoles, Set<RoleModel> newRoles, boolean unlinkFromOtherRoles) {
        AuthorityModel result = roleAuthServiceHelper.createSubWithLinks(
                model,
                existingRoles,
                newRoles,
                unlinkFromOtherRoles,
                m -> roleRepository.findByRole(m.getName()),
                RoleJpa::getAuthorities
        );
        refreshRoleCache(getChangedRoles(existingRoles, newRoles, unlinkFromOtherRoles));
        return result;
    }

    private Set<RoleModel> getChangedRoles(Set<RoleModel> existingRoles, Set<RoleModel> newRoles, boolean unlinkFromOtherRoles) {
        return unlinkFromOtherRoles ? CollectionUtils.disjunction(existingRoles, newRoles) : newRoles;
    }

    @Transactional
    @CachePut(value = "roles", key = "#role.name")
    public RoleModel saveRole(RoleModel role) {
        return roleAuthServiceHelper.saveOrUpdateEntityFoundBy(role, () -> findByRoleName(role));
    }

    private Optional<RoleJpa> findByRoleName(RoleModel role) {
        return roleRepository.findByRole(role.getName());
    }

    private void refreshRoleCache(Set<RoleModel> roles) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache("roles"));
        for (RoleModel role : roles) {
            roleRepository.findByRole(role.getName())
                    .map(RoleModel::new)
                    .ifPresent(fresh -> cache.put(role.getName(), fresh));
        }
    }
}
