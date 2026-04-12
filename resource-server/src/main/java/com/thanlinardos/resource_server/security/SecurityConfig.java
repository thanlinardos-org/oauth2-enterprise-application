package com.thanlinardos.resource_server.security;

import com.thanlinardos.resource_server.model.mapped.RoleModel;
import com.thanlinardos.resource_server.service.role.api.OauthRoleService;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.base.Authority;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.security.SecurityCommonConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class SecurityConfig extends SecurityCommonConfig<RoleModel> {

    private final OauthRoleService roleService;

    public SecurityConfig(OauthRoleService roleService) {
        super(roleService);
        this.roleService = roleService;
    }

    @Override
    protected Collection<Authority> getAuthorities() {
        return roleService.getAllAuthorities();
    }

    @Bean
    @Order(1)
    @Override
    protected SecurityFilterChain userLoginSecurityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults());
        return super.userLoginSecurityFilterChain(http);
    }
}
