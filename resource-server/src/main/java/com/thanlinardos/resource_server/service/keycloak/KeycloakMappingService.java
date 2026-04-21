package com.thanlinardos.resource_server.service.keycloak;

import com.thanlinardos.resource_server.misc.utils.RoleUtils;
import com.thanlinardos.resource_server.model.info.Client;
import com.thanlinardos.resource_server.model.info.Customer;
import com.thanlinardos.resource_server.model.info.OwnerType;
import com.thanlinardos.resource_server.model.mapped.OwnerModel;
import com.thanlinardos.resource_server.model.mapped.RoleModel;
import com.thanlinardos.resource_server.security.keycloak.KeycloakServiceUtils;
import com.thanlinardos.resource_server.service.role.api.OauthRoleService;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.base.PrivilegedResource;
import com.thanlinardos.spring_enterprise_library.time.TimeFactory;
import com.thanlinardos.spring_enterprise_library.time.utils.DateUtils;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.AbstractUserRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.thanlinardos.spring_enterprise_library.time.utils.DateUtils.getLocalDateFromEpochMilli;

@Service
@Slf4j
public class KeycloakMappingService {

    private static final String KEYCLOAK = "KEYCLOAK";

    private final OauthRoleService roleService;

    public KeycloakMappingService(OauthRoleService roleService) {
        this.roleService = roleService;
    }

    public OwnerModel mapUserRepresentationToOwner(UserRepresentation user, List<RoleRepresentation> userRoles, @Nullable Long ownerId, @Nullable Long customerId) {
        Set<RoleModel> roleModels = getRoleModels(userRoles);
        int privilegeLevel = PrivilegedResource.calcPrivilegeLvlFromRoles(roleModels);
        LocalDateTime createdAt = DateUtils.getLocalDateTimeFromEpochMilli(user.getCreatedTimestamp());
        return OwnerModel.builder() //NOSONAR (S3252)
                .id(ownerId)
                .uuid(UUID.fromString(user.getId()))
                .principalName(user.getEmail())
                .roles(roleModels)
                .privilegeLevel(privilegeLevel)
                .customer(KeycloakServiceUtils.mapUserRepresentationToCustomerModel(user, privilegeLevel, customerId))
                .type(OwnerType.CUSTOMER)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .createdBy(KEYCLOAK)
                .updatedBy(KEYCLOAK)
                .build();
    }

    public Customer mapUserRepresentationToCustomer(List<RoleRepresentation> userRoles, UserRepresentation user) {
        LocalDate createdAt = getLocalDateFromEpochMilli(user.getCreatedTimestamp());
        return Customer.builder() //NOSONAR (S3252)
                .name(user.getUsername())
                .email(user.getEmail())
                .createDt(createdAt)
                .roles(getRoleModels(userRoles))
                .build();
    }

    public Client mapClientRepresentationToClient(List<RoleRepresentation> userRoles, ClientRepresentation client, UserRepresentation serviceAccountUser) {
        LocalDate createdAt = getLocalDateFromEpochMilli(serviceAccountUser.getCreatedTimestamp());
        return Client.builder() //NOSONAR (S3252)
                .name(client.getClientId())
                .createDt(createdAt)
                .roles(getRoleModels(userRoles))
                .serviceAccountId(UUID.fromString(serviceAccountUser.getId()))
                .build();
    }

    public OwnerModel mapClientRepresentationToOwnerModel(@Nullable Long ownerId,
                                                          @Nullable Long clientId,
                                                          ClientRepresentation client,
                                                          List<RoleRepresentation> roles,
                                                          Optional<UserRepresentation> serviceAccountUser,
                                                          Collection<RoleModel> serviceAccountRoles) {
        Set<RoleModel> clientRoles = new HashSet<>(getRoleModels(roles));
        clientRoles.addAll(serviceAccountRoles);
        int privilegeLevel = PrivilegedResource.calcPrivilegeLvlFromRoles(clientRoles);
        LocalDateTime createdAt = getServiceAccountUserCreatedAtOrNow(serviceAccountUser);
        String serviceAccountId = getServiceAccountId(serviceAccountUser);

        return OwnerModel.builder() //NOSONAR (S3252)
                .id(ownerId)
                .uuid(UUID.fromString(client.getId()))
                .principalName(client.getClientId())
                .roles(clientRoles)
                .privilegeLevel(privilegeLevel)
                .client(KeycloakServiceUtils.mapClientRepresentationToClientModel(client, privilegeLevel, createdAt, serviceAccountId, clientId))
                .type(OwnerType.CLIENT)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .createdBy(KEYCLOAK)
                .updatedBy(KEYCLOAK)
                .build();
    }

    private LocalDateTime getServiceAccountUserCreatedAtOrNow(Optional<UserRepresentation> serviceAccountUser) {
        return serviceAccountUser
                .map(UserRepresentation::getCreatedTimestamp)
                .map(DateUtils::getLocalDateTimeFromEpochMilli)
                .orElse(TimeFactory.getDateTime());
    }

    @Nullable
    private String getServiceAccountId(Optional<UserRepresentation> serviceAccountUser) {
        return serviceAccountUser
                .map(AbstractUserRepresentation::getId)
                .orElse(null);
    }

    public Set<RoleModel> getRoleModels(List<RoleRepresentation> roles) {
        Set<String> roleNames = RoleUtils.getRoleNamesFromRoleRepresentations(roles);
        return roleService.findRolesWithoutValidation(roleNames);
    }
}
