package com.thanlinardos.resource_server.service;

import com.thanlinardos.resource_server.TestUtils;
import com.thanlinardos.resource_server.WithMockCustomUser;
import com.thanlinardos.resource_server.model.info.OwnerType;
import com.thanlinardos.resource_server.model.mapped.CustomerModel;
import com.thanlinardos.resource_server.model.mapped.OwnerModel;
import com.thanlinardos.resource_server.service.owner.OwnerService;
import com.thanlinardos.resource_server.service.role.RoleCacheService;
import com.thanlinardos.resource_server.service.role.api.OauthRoleService;
import com.thanlinardos.spring_enterprise_library.annotations.SpringTest;
import com.thanlinardos.spring_enterprise_library.time.TimeFactory;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static com.thanlinardos.resource_server.TestUtils.ROLE_ADMIN;
import static com.thanlinardos.resource_server.TestUtils.ROLE_MANAGER;
import static com.thanlinardos.resource_server.TestUtils.ROLE_OWNER;
import static com.thanlinardos.resource_server.TestUtils.ROLE_USER;
import static com.thanlinardos.resource_server.TestUtils.buildRole;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@SpringTest
@WithMockCustomUser()
@Sql(scripts = {"classpath:db/h2/initRoleData.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {"classpath:db/h2/initUserData.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class OwnerServiceTest {

    @Autowired
    private OauthRoleService roleService;
    @Autowired
    private OwnerService ownerService;
    @Autowired
    private RoleCacheService roleCacheService;
    @Autowired
    private Environment environment;

    @Test
    @Sql(scripts = {"classpath:db/h2/clearUserData.sql"},
            config = @SqlConfig(transactionMode = ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithMockCustomUser(name = "admin@email.com", roles = {ROLE_USER, ROLE_MANAGER, ROLE_ADMIN, ROLE_OWNER})
    void save() {
        OwnerModel expected = OwnerModel.builder() //NOSONAR (S3252)
                .type(OwnerType.CUSTOMER)
                .uuid(UUID.randomUUID())
                .principalName("test@email.com")
                .customer(TestUtils.buildTestCustomer("test@email.com"))
                .privilegeLevel(1)
                .roles(Set.of(roleService.findRole(ROLE_USER), roleService.findRole(ROLE_MANAGER), roleService.findRole(ROLE_ADMIN)))
                .createdAt(TimeFactory.getDateTime())
                .createdBy("test")
                .updatedAt(TimeFactory.getDateTime())
                .updatedBy("test")
                .build();
        OwnerModel actual = ownerService.save(expected);
        Assertions.assertEquals(expected, actual);
    }

    public static Stream<Arguments> getOwnerByNameParams() {
        return Stream.of(
                Arguments.of("Simple user",
                        "user@email.com",
                        null,
                        TestUtils.buildTestOwner("user@email.com", Set.of(buildRole(ROLE_USER))),
                        ""),
                Arguments.of("Manager, no rights",
                        "manager@email.com",
                        IllegalAccessException.class,
                        null,
                        "[OwnerService.getOwnerByName(..)] Unauthorized Access to Resource: %s for user: " + TestUtils.DEFAULT_USER),
                Arguments.of("Admin, no rights",
                        "admin@email.com",
                        IllegalAccessException.class,
                        null,
                        "[OwnerService.getOwnerByName(..)] Unauthorized Access to Resource: %s for user: " + TestUtils.DEFAULT_USER),
                Arguments.of("User not found",
                        "nonexistent",
                        null,
                        null,
                        "")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getOwnerByNameParams")
    @Transactional
    void getOwnerByName(String description, String email, Class<?> exceptionType, OwnerModel expected, String message) {
        if (exceptionType == null) {
            OwnerModel actual = ownerService.getOwnerByName(email).orElse(null);
            assertThatOwnerEqualsOrNull(expected, actual);
        } else {
            UndeclaredThrowableException exception = Assertions.assertThrows(UndeclaredThrowableException.class, () -> ownerService.getOwnerByName(email));
            Throwable cause = exception.getCause();
            Assertions.assertEquals(exceptionType, cause.getClass());
            Assertions.assertTrue(cause.getMessage().contains(String.format(message, email)));
        }
    }

    private static void assertThatOwnerEqualsOrNull(@Nullable OwnerModel expected, @Nullable OwnerModel actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else {
            assertThatOwnerEquals(expected, actual);
        }
    }

    private static void assertThatOwnerEquals(OwnerModel expected, OwnerModel actual) {
        Assertions.assertNotNull(actual);

        Assertions.assertEquals(expected.getPrincipalName(), actual.getPrincipalName());
        Assertions.assertEquals(expected.getRoleNames(), actual.getRoleNames());
        Assertions.assertEquals(expected.getPrivilegeLevel(), actual.getPrivilegeLevel());
        Assertions.assertEquals(expected.getType(), actual.getType());

        CustomerModel actualCustomer = actual.getCustomer();
        CustomerModel expectedCustomer = expected.getCustomer();
        Assertions.assertNotNull(actualCustomer);
        Assertions.assertNotNull(expectedCustomer);

        Assertions.assertEquals(expectedCustomer.getUsername(), actualCustomer.getUsername());
        Assertions.assertEquals(expectedCustomer.getEmail(), actualCustomer.getEmail());
    }

}