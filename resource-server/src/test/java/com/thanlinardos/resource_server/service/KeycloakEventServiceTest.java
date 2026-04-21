package com.thanlinardos.resource_server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanlinardos.resource_server.batch.keycloak.event.KeycloakAdminEventModel;
import com.thanlinardos.resource_server.model.mapped.RoleModel;
import com.thanlinardos.resource_server.service.keycloak.KeycloakEventService;
import com.thanlinardos.resource_server.service.role.api.OauthRoleService;
import com.thanlinardos.resource_server.service.task.TaskRunService;
import com.thanlinardos.spring_enterprise_library.annotations.CoreTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.AdminEventRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@CoreTest
class KeycloakEventServiceTest {

    @Spy private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private TaskRunService taskRunService;
    @Mock private OauthRoleService roleService;
    @Mock private Keycloak keycloak = mockKeycloak();

    @InjectMocks
    private KeycloakEventService keycloakEventService;

    @Test
    void parseRolesFromEvent() throws JsonProcessingException {
        AdminEventRepresentation event = new AdminEventRepresentation();
        event.setId(UUID.randomUUID().toString());
        event.setOperationType("CREATE");
        event.setResourceType("REALM_ROLE_MAPPING");
        event.setTime(System.currentTimeMillis());
        event.setRealmId(UUID.randomUUID().toString());
        event.setResourcePath("users/" + UUID.randomUUID());

        RoleRepresentation role1 = new RoleRepresentation();
        role1.setName("GUEST");
        role1.setId(UUID.randomUUID().toString());

        RoleRepresentation role2 = new RoleRepresentation();
        role2.setName("USER");
        role2.setId(UUID.randomUUID().toString());

        event.setRepresentation(objectMapper.writeValueAsString(Set.of(role1, role2)));
        KeycloakAdminEventModel eventRepresentation = new KeycloakAdminEventModel(event);

        Collection<RoleModel> actual = keycloakEventService.parseRolesFromEvent(eventRepresentation);
        Assertions.assertNotNull(actual);
    }

    private Keycloak mockKeycloak() {
        Keycloak mocked = mock(Keycloak.class);
        when(mocked.realm(any())).thenReturn(mock(RealmResource.class));
        return mocked;
    }
}