package com.thanlinardos.resource_server.service.role;

import com.thanlinardos.resource_server.aspect.annotation.ExcludeFromLoggingAspect;
import com.thanlinardos.resource_server.model.constants.SecurityConstants;
import com.thanlinardos.resource_server.model.info.AuthorityInfo;
import com.thanlinardos.resource_server.model.info.RoleInfo;
import com.thanlinardos.resource_server.model.mapped.AuthorityModel;
import com.thanlinardos.resource_server.model.mapped.RoleModel;
import com.thanlinardos.resource_server.service.role.api.OauthRoleService;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.base.Authority;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.thanlinardos.spring_enterprise_library.objects.utils.FunctionUtils.stream;
import static com.thanlinardos.spring_enterprise_library.objects.utils.PredicateUtils.isContainedIn;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements OauthRoleService {

    private final RoleCacheService roleCacheService;

    @Override
    public Collection<RoleModel> getAllRoles() {
        return roleCacheService.getAllRoles();
    }

    @Override
    public RoleModel findRole(String name) {
        return roleCacheService.getRoleByName(name);
    }

    @Override
    public Set<RoleModel> findRoles(Collection<String> names) {
        return findRoleStream(names)
                .collect(Collectors.toSet());
    }

    public Set<RoleModel> findRolesWithoutValidation(Collection<String> names) {
        return findRoleStream(names, false)
                .collect(Collectors.toSet());
    }

    @Override
    public int getPrivilegeLevelFromRoleNames(Collection<String> names) {
        return findRoleStream(names)
                .map(RoleModel::getPrivilegeLvl)
                .min(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);
    }

    @Override
    public Set<RoleModel> getRolesIncludingHigherPrivilegeLvlRoles(Collection<String> names) {
        Set<RoleModel> roles = new HashSet<>(findRoles(names));
        Integer privilegeLvl = getMinPrivilegeLvl(roles);
        roles.addAll(findRolesWithHigherPrivilegeLvl(privilegeLvl));
        return roles;
    }

    @Override
    @ExcludeFromLoggingAspect
    public Collection<GrantedAuthority> findGrantedAuthoritiesWithRole(RoleModel role) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>(role.getGrantedAuthorities());
        grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
        return grantedAuthorities;
    }

    @Override
    public Collection<GrantedAuthority> findGrantedAuthoritiesWithRoles(Collection<String> roleNames) {
        return findRoleStream(roleNames)
                .flatMap(stream(this::findGrantedAuthoritiesWithRole))
                .toList();
    }

    @Override
    public Collection<Authority> getAllAuthorities() {
        return roleCacheService.getAllAuthorities();
    }

    public Set<AuthorityModel> findAuthoritiesByNames(Set<String> names) {
        return getAllAuthorities().stream()
                .filter(isContainedIn(Authority::getName, names))
                .map(AuthorityModel.class::cast)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public AuthorityModel createAuthorityWithRoles(AuthorityInfo authorityInfo, boolean unlinkFromOtherRoles) {
        AuthorityModel model = roleCacheService.saveAuthority(AuthorityModel.fromAuthorityInfo(authorityInfo));
        Set<RoleModel> newRoles = getRolesIncludingHigherPrivilegeLvlRoles(authorityInfo.roles());
        return roleCacheService.createAuthorityWithRoles(
                model,
                getExistingRolesWithAuthority(model),
                newRoles,
                unlinkFromOtherRoles);
    }

    @Override
    @Transactional
    public RoleModel createRoleWithAuthorities(RoleInfo roleInfo, boolean unlinkFromOtherAuthorities) {
        RoleModel model = roleCacheService.saveRole(RoleModel.fromRoleInfo(roleInfo));
        Set<AuthorityModel> newAuthorities = getAuthoritiesIncludingUser(roleInfo, model);
        return roleCacheService.createRoleWithAuthorities(model, newAuthorities, unlinkFromOtherAuthorities);
    }

    private Set<AuthorityModel> getAuthoritiesIncludingUser(RoleInfo roleInfo, RoleModel model) {
        Set<AuthorityModel> newAuthorities = findAuthoritiesByNames(roleInfo.authorities());
        if (model.getPrivilegeLvl() <= 2) {
            newAuthorities.addAll(findRole(SecurityConstants.DEFAULT_USER_ROLE).getAuthorities());
        }
        return newAuthorities;
    }

    private Set<RoleModel> getExistingRolesWithAuthority(AuthorityModel m) {
        return getAllRoles().stream()
                .filter(role -> role.getAuthorities().contains(m))
                .collect(Collectors.toSet());
    }

    private Stream<RoleModel> findRoleStream(Collection<String> names) {
        return findRoleStream(names, true);
    }

    private Stream<RoleModel> findRoleStream(Collection<String> names, boolean shouldValidateRoles) {
        return names.stream()
                .map(roleCacheService::getRoleByName)
                .map(role -> shouldValidateRoles ? Objects.requireNonNull(role) : role)
                .filter(role -> shouldValidateRoles || Objects.nonNull(role));
    }

    private Set<RoleModel> findRolesWithHigherPrivilegeLvl(int privilegeLvl) {
        return getAllRoles().stream()
                .filter(role -> role.getPrivilegeLvl() > privilegeLvl)
                .collect(Collectors.toSet());
    }

    private Integer getMinPrivilegeLvl(Set<RoleModel> roles) {
        return roles.stream()
                .map(RoleModel::getPrivilegeLvl)
                .min(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);
    }
}
