package com.thanlinardos.resource_server.model.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class SecurityConstants {

    public static final String AUTH_NAME_AND_AUTH_FORMAT = "#username == authentication.name and hasAuthority('%s')";
    public static final String OR_ADMIN_FORMAT = "hasRole('ADMIN') or (%s)";
    public static final String OR_MANAGER_FORMAT = OR_ADMIN_FORMAT.formatted("hasRole('MANAGER') or (%s)");

    public static final Set<String> DEFAULT_GUEST_ROLES = Set.of("ROLE_GUEST");
    public static final Set<String> DEFAULT_GUEST_ROLES_NO_PREFIX = Set.of("GUEST");
    public static final String DEFAULT_USER_ROLE = "ROLE_USER";
}
