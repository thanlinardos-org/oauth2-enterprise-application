package com.thanlinardos.resource_server.controller.rest;

import com.thanlinardos.resource_server.model.info.AuthorityInfo;
import com.thanlinardos.resource_server.model.mapped.AuthorityModel;
import com.thanlinardos.resource_server.model.mapped.RoleModel;
import com.thanlinardos.resource_server.service.role.RoleCacheService;
import com.thanlinardos.resource_server.service.role.api.OauthRoleService;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.base.Authority;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class AuthorityController {

    private final RoleCacheService roleCacheService;
    private final OauthRoleService roleService;

    @PostMapping("/authorities")
    public ResponseEntity<Authority> createAuthority(@RequestBody AuthorityInfo authorityInfo) {
        AuthorityModel model = fromAuthorityInfo(authorityInfo);
        model = (AuthorityModel) roleCacheService.addAuthority(model);
        Set<RoleModel> roles = roleService.getRolesIncludingLowerPrivilegeLvlRoles(authorityInfo.roles());

        for (RoleModel role : roles) {
            roleCacheService.linkAuthorityToRole(model.getId(), role.getName());
        }
        return ResponseEntity.ok(model);
    }

    private AuthorityModel fromAuthorityInfo(AuthorityInfo authorityInfo) {
        return AuthorityModel.builder()
                .name(authorityInfo.name())
                .access(authorityInfo.access())
                .uri(authorityInfo.uri())
                .expression(authorityInfo.expression())
                .build();
    }
}
