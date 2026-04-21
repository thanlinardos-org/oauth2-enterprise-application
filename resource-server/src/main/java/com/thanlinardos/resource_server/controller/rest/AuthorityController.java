package com.thanlinardos.resource_server.controller.rest;

import com.thanlinardos.resource_server.model.info.AuthorityInfo;
import com.thanlinardos.resource_server.model.info.RoleInfo;
import com.thanlinardos.resource_server.model.mapped.RoleModel;
import com.thanlinardos.resource_server.service.role.api.OauthRoleService;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.base.Authority;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthorityController {

    private final OauthRoleService roleService;

    @PostMapping("/authorities")
    public ResponseEntity<Authority> createAuthority(@RequestBody AuthorityInfo authorityInfo, @RequestParam(required = false, defaultValue = "false") boolean unlinkFromOtherRoles) {
        return ResponseEntity.ok(roleService.createAuthorityWithRoles(authorityInfo, unlinkFromOtherRoles));
    }

    @PostMapping("/roles")
    public ResponseEntity<RoleModel> createRole(@RequestBody RoleInfo roleInfo, @RequestParam(required = false, defaultValue = "false") boolean unlinkFromOtherAuthorities) {
        return ResponseEntity.ok(roleService.createRoleWithAuthorities(roleInfo, unlinkFromOtherAuthorities));
    }
}
