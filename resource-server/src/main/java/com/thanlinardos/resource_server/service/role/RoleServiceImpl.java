package com.thanlinardos.resource_server.service.role;

import com.thanlinardos.resource_server.aspect.annotation.ExcludeFromLoggingAspect;
import com.thanlinardos.resource_server.model.mapped.RoleModel;
import com.thanlinardos.resource_server.service.role.api.OauthRoleService;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.base.Authority;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.thanlinardos.spring_enterprise_library.objects.utils.FunctionUtils.stream;

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
    public Set<RoleModel> getRolesIncludingLowerPrivilegeLvlRoles(Collection<String> names) {
        Set<RoleModel> roles = new HashSet<>(findRoles(names));
        Integer privilegeLvl = getMinPrivilegeLvl(roles);
        roles.addAll(findRolesWithLowerPrivilegeLvl(privilegeLvl));
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

    private Stream<RoleModel> findRoleStream(Collection<String> names) {
        return findRoleStream(names, true);
    }

    private Stream<RoleModel> findRoleStream(Collection<String> names, boolean shouldValidateRoles) {
        return names.stream()
                .map(roleCacheService::getRoleByName)
                .map(role -> shouldValidateRoles ? Objects.requireNonNull(role) : role)
                .filter(role -> shouldValidateRoles || Objects.nonNull(role));
    }

    private Set<RoleModel> findRolesWithLowerPrivilegeLvl(int privilegeLvl) {
        return getAllRoles().stream()
                .filter(role -> role.getPrivilegeLvl() < privilegeLvl)
                .collect(Collectors.toSet());
    }

    private Integer getMinPrivilegeLvl(Set<RoleModel> roles) {
        return roles.stream()
                .map(RoleModel::getPrivilegeLvl)
                .min(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);
    }
}
