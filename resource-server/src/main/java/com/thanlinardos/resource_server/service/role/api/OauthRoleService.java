package com.thanlinardos.resource_server.service.role.api;

import com.thanlinardos.resource_server.model.mapped.RoleModel;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.api.service.RoleService;

import java.util.Collection;
import java.util.Set;

public interface OauthRoleService extends RoleService<RoleModel> {

    Set<RoleModel> getRolesIncludingLowerPrivilegeLvlRoles(Collection<String> names);

    @Override
    Set<RoleModel> findRolesWithoutValidation(Collection<String> names);
}
