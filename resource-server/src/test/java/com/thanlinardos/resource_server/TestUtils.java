package com.thanlinardos.resource_server;

import com.thanlinardos.resource_server.model.info.OwnerType;
import com.thanlinardos.resource_server.model.mapped.CustomerModel;
import com.thanlinardos.resource_server.model.mapped.OwnerModel;
import com.thanlinardos.resource_server.model.mapped.RoleModel;
import com.thanlinardos.spring_enterprise_library.time.TimeFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class TestUtils implements WithSecurityContextFactory<WithMockCustomUser> {

    public static final String DEFAULT_USER = "user@email.com";

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_OWNER = "ROLE_OWNER";
    public static final GrantedAuthority[] USER_AUTHORITIES = {
            new SimpleGrantedAuthority(ROLE_USER),
            new SimpleGrantedAuthority("READ_USER"),
            new SimpleGrantedAuthority("READ_OWNER"),
            new SimpleGrantedAuthority("READ_CUSTOMERS_USERNAME")
    };
    public static final GrantedAuthority[] MANAGER_AUTHORITIES = {
            new SimpleGrantedAuthority(ROLE_MANAGER),
            new SimpleGrantedAuthority("READ_USER"),
            new SimpleGrantedAuthority("READ_OWNER"),
            new SimpleGrantedAuthority("READ_CUSTOMERS_USERNAME")
    };
    public static final GrantedAuthority[] ADMIN_AUTHORITIES = {
            new SimpleGrantedAuthority(ROLE_ADMIN),
            new SimpleGrantedAuthority("READ_USER"),
            new SimpleGrantedAuthority("READ_OWNER"),
            new SimpleGrantedAuthority("READ_CUSTOMERS_USERNAME"),
            new SimpleGrantedAuthority("ALL_ADMIN"),
            new SimpleGrantedAuthority("CREATE_CUSTOMER"),
    };
    public static final GrantedAuthority[] OWNER_AUTHORITIES = {
            new SimpleGrantedAuthority(ROLE_OWNER),
            new SimpleGrantedAuthority("READ_USER"),
            new SimpleGrantedAuthority("READ_OWNER"),
            new SimpleGrantedAuthority("READ_CUSTOMERS_USERNAME"),
            new SimpleGrantedAuthority("ALL_ADMIN"),
            new SimpleGrantedAuthority("CREATE_CUSTOMER"),
    };

    public static final Jwt ADMIN_PRINCIPAL = Jwt.withTokenValue("admin")
            .header("alg", "none")
            .claim("sub", "admin")
            .claim("email", "admin@email.com")
            .claim("resource_access", List.of("account"))
            .build();

    public static final Jwt USER_PRINCIPAL = Jwt.withTokenValue("user")
            .header("alg", "none")
            .claim("sub", "user")
            .claim("email", "user@email.com")
            .claim("resource_access", List.of("account"))
            .build();

    public static void autoAuthenticateAdminUser() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(ADMIN_PRINCIPAL, "pass",
                List.of(new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_MANAGER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void autoAuthenticateUser() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(USER_PRINCIPAL, "pass",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static CustomerModel buildTestCustomer(String email) {
        return CustomerModel.builder() //NOSONAR (S3252)
                .username(email.split("@")[0])
                .email(email)
                .firstName("test")
                .lastName("test")
                .privilegeLevel(1)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .createdAt(TimeFactory.getDateTime())
                .createdBy("test")
                .updatedAt(TimeFactory.getDateTime())
                .updatedBy("test")
                .mobileNumber("1234567890")
                .build();
    }

    public static OwnerModel buildTestOwner(String email, Set<RoleModel> roles) {
        return OwnerModel.builder() //NOSONAR (S3252)
                .type(OwnerType.CUSTOMER)
                .principalName(email)
                .customer(TestUtils.buildTestCustomer(email))
                .privilegeLevel(3)
                .roles(roles)
                .build();
    }

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Jwt principal = Jwt.withTokenValue(customUser.name())
                .header("alg", "none")
                .claim("sub", customUser.name())
                .claim("email", customUser.name())
                .claim("resource_access", Map.of("account", "account"))
                .build();
        Authentication auth = UsernamePasswordAuthenticationToken.authenticated(principal,
                customUser.password(),
                Arrays.stream(customUser.roles())
                        .map(TestUtils::buildRole)
                        .flatMap(TestUtils::getGrantedAuthorityStreamIncludingRoleName)
                        .distinct()
                        .toList()
        );
        context.setAuthentication(auth);
        return context;
    }

    private static Stream<GrantedAuthority> getGrantedAuthorityStreamIncludingRoleName(RoleModel role) {
        return switch (role.getName()) {
            case ROLE_USER -> Arrays.stream(USER_AUTHORITIES);
            case ROLE_MANAGER -> Arrays.stream(MANAGER_AUTHORITIES);
            case ROLE_ADMIN -> Arrays.stream(ADMIN_AUTHORITIES);
            case ROLE_OWNER -> Arrays.stream(OWNER_AUTHORITIES);
            default -> Stream.empty();
        };
    }

    public static RoleModel buildRole(String name) {
        return RoleModel.builder() //NOSONAR (S3252)
                .role(name)
                .build();
    }
}
