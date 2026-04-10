package com.thanlinardos.resource_server.service.user;

import com.thanlinardos.resource_server.model.constants.SecurityConstants;
import com.thanlinardos.resource_server.model.info.Client;
import com.thanlinardos.resource_server.model.info.Customer;
import com.thanlinardos.resource_server.model.info.OwnerType;
import com.thanlinardos.resource_server.model.info.RegisterCustomerDetails;
import com.thanlinardos.resource_server.model.mapped.ClientModel;
import com.thanlinardos.resource_server.model.mapped.CustomerModel;
import com.thanlinardos.resource_server.model.mapped.OwnerModel;
import com.thanlinardos.resource_server.model.mapped.RoleModel;
import com.thanlinardos.resource_server.security.keycloak.KeycloakServiceUtils;
import com.thanlinardos.resource_server.service.keycloak.KeycloakMappingService;
import com.thanlinardos.resource_server.service.owner.OwnerService;
import com.thanlinardos.resource_server.service.user.api.UserService;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.types.OperationType;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.utils.ModelUtils;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.thanlinardos.resource_server.security.keycloak.KeycloakServiceUtils.handleRequest;
import static com.thanlinardos.spring_enterprise_library.objects.utils.StreamUtils.findExactlyOne;
import static com.thanlinardos.spring_enterprise_library.parse.utils.ParserUtil.getPathParameterFromLocationURI;

@Slf4j
public class KeycloakUserService implements UserService {

    private final OwnerService ownerService;
    private final RealmResource realm;
    private final KeycloakMappingService keycloakMappingService;

    public KeycloakUserService(OwnerService ownerService, RealmResource keycloakRealm, KeycloakMappingService keycloakMappingService) {
        this.ownerService = ownerService;
        this.keycloakMappingService = keycloakMappingService;
        log.info("KeycloakUserService created.");
        realm = keycloakRealm;
    }

    public OwnerModel mapUserResourceToOwner(UserRepresentation userRepresentation) {
        UserResource userResource = realm.users().get(userRepresentation.getId());
        return keycloakMappingService.mapUserRepresentationToOwner(userRepresentation, userResource.roles().realmLevel().listAll(), null, null);
    }

    @Override
    public List<CustomerModel> syncAndGetAllCustomers(boolean shouldUpdateExisting) {
        return realm.users().list().stream()
                .map(this::mapUserResourceToOwner)
                .map(shouldUpdateExisting ? ownerService::save : ownerService::saveIfNotExistsByUuid)
                .map(OwnerModel::getCustomer)
                .toList();
    }

    @Override
    public List<ClientModel> syncAndGetAllClients(boolean shouldUpdateExisting) {
        return realm.clients().findAll().stream()
                .map(ClientRepresentation::getId)
                .map(this::findClientById)
                .map(this::mapClientResourceToOwnerModel)
                .map(shouldUpdateExisting ? ownerService::save : ownerService::saveIfNotExistsByUuid)
                .map(OwnerModel::getClient)
                .toList();
    }

    private OwnerModel mapClientResourceToOwnerModel(ClientResource client) {
        return mapClientResourceToOwnerModel(client, getServiceAccountRoles(client), client.toRepresentation());
    }

    @Nonnull
    private UserResource findUserById(String id) {
        return Objects.requireNonNull(realm.users().get(id));
    }

    @Transactional
    @Override
    public String createGuestCustomer(RegisterCustomerDetails customerDetails) {
        UserRepresentation user = mapCustomerDetailsToUserRepresentation(customerDetails);
        String userId = createKeycloakUserOrThrow(user);

        addRealmRolesToUserOrThrow(userId, SecurityConstants.DEFAULT_GUEST_ROLES_NO_PREFIX);
        updatePasswordCredentialToUserOrThrow(userId, customerDetails.password());
        return persistNewGuestOwnerOrRollbackAndThrow(userId, customerDetails.email());
    }

    private String persistNewGuestOwnerOrRollbackAndThrow(String userId, String email) {
        try {
            return getGuestOwnerByEmailAndPersist(email);
        } catch (Exception e) {
            rollbackOnNewUserPersistenceError(e, userId);
            throw e;
        }
    }

    private UserRepresentation mapCustomerDetailsToUserRepresentation(RegisterCustomerDetails customerDetails) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(customerDetails.email());
        user.setEmail(customerDetails.email());
        user.setEnabled(true);
        user.setFirstName(customerDetails.firstName());
        user.setLastName(customerDetails.lastName());
        user.setAttributes(Map.of("mobileNumber", Collections.singletonList(customerDetails.mobileNumber())));
        return user;
    }

    @Transactional
    @Override
    public OwnerModel createCustomerWithRoles(Customer customer) {
        UserRepresentation user = mapCustomerDetailsToUserRepresentation(customer.toRegisterCustomerDetails());
        String userId = createKeycloakUserOrThrow(user);
        Set<String> realmRoles = customer.getRoleNames();

        addRealmRolesToUserOrThrow(userId, realmRoles);
        updatePasswordCredentialToUserOrThrow(userId, customer.getPassword());
        return persistNewOwnerOrRollbackAndThrow(userId, customer.getEmail());
    }

    private OwnerModel persistNewOwnerOrRollbackAndThrow(String userId, String email) {
        try {
            return getOwnerByEmailAndPersist(email);
        } catch (Exception e) {
            rollbackOnNewUserPersistenceError(e, userId);
            throw e;
        }
    }

    private void rollbackOnNewUserPersistenceError(Exception e, String userId) {
        log.error("Error persisting new user from keycloak with uuid: {}", userId, e);
        try (Response ignored = KeycloakServiceUtils.handleRequest(realm.users()::delete, userId, OperationType.DELETE)) {
            log.info("Successfully rolled back by deleting user from keycloak with uuid: {}", userId);
        } catch (Exception ex) {
            log.error("Failed to roll back by deleting user from keycloak with uuid: {}", userId, ex);
        }
    }

    private String createKeycloakUserOrThrow(UserRepresentation user) {
        Response response = KeycloakServiceUtils.handleRequest(realm.users()::create, user, OperationType.CREATE);
        return getPathParameterFromLocationURI(response);
    }

    private void addRealmRolesToUserOrThrow(String userId, Set<String> roles) {
        List<RoleRepresentation> roleRepresentations = roles.stream()
                .map(role -> realm.roles().get(role).toRepresentation())
                .toList();
        UserResource user = findUserById(userId);
        handleRequest(user.roles().realmLevel()::add, roleRepresentations, OperationType.ADD);
    }

    private void updatePasswordCredentialToUserOrThrow(String userId, String password) {
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);
        UserResource user = findUserById(userId);
        handleRequest(user::resetPassword, passwordCred, OperationType.UPDATE);
    }

    @Override
    public CustomerModel getCustomerByUsernameAndPersist(String username) {
        OwnerModel ownerModel = mapAndPersistUserResourceToOwnerModel(getUserRepresentationByUsername(username));
        return ownerModel.getCustomer();
    }

    @Override
    public CustomerModel getCustomerByEmailAndPersist(String email) {
        return getOwnerByEmailAndPersist(email).getCustomer();
    }

    private OwnerModel getOwnerByEmailAndPersist(String email) {
        return mapAndPersistUserResourceToOwnerModel(getUserRepresentationByEmail(email));
    }

    @Override
    public OwnerModel getOwnerByIdAndPersistOrUpdate(UUID uuid, OwnerType ownerType) {
        OwnerModel owner = ownerService.getOwnerByUuid(uuid)
                .orElse(null);
        Long ownerId = ModelUtils.getIdFromModel(owner);
        Long resourceId = ModelUtils.getIdFromNestedModelOr(owner, OwnerModel::getCustomer, OwnerModel::getClient);

        return switch (ownerType) {
            case CUSTOMER -> mapAndPersistUserResourceAsOwner(uuid, ownerId, resourceId);
            case CLIENT -> mapAndPersistClientResourceAsOwner(uuid, ownerId, resourceId);
        };
    }

    private OwnerModel mapAndPersistClientResourceAsOwner(UUID uuid, @Nullable Long ownerId, @Nullable Long resourceId) {
        ClientResource client = findClientByUuid(uuid);
        OwnerModel ownerModel = keycloakMappingService.mapClientRepresentationToOwnerModel(ownerId, resourceId, client.toRepresentation(), client.roles().list(), KeycloakServiceUtils.getServiceAccountUser(client), getServiceAccountRoles(client));
        return saveGuestOrOwner(ownerModel);
    }

    private OwnerModel mapAndPersistUserResourceAsOwner(UUID uuid, @Nullable Long ownerId, @Nullable Long resourceId) {
        UserResource user = findUserById(uuid.toString());
        OwnerModel ownerModel = keycloakMappingService.mapUserRepresentationToOwner(user.toRepresentation(), getUserRoles(user), ownerId, resourceId);
        return saveGuestOrOwner(ownerModel);
    }

    @Nonnull
    private ClientResource findClientByUuid(UUID uuid) {
        return Objects.requireNonNull(realm.clients().get(uuid.toString()));
    }

    @Nonnull
    private ClientResource findClientById(String id) {
        return Objects.requireNonNull(realm.clients().get(id));
    }

    private Set<RoleModel> getServiceAccountRoles(ClientResource clientResource) {
        try {
            String serviceAccountId = clientResource.getServiceAccountUser().getId();
            UserResource user = findUserById(serviceAccountId);
            return keycloakMappingService.getRoleModels(getUserRoles(user));
        } catch (BadRequestException e) {
            return Collections.emptySet();
        }
    }

    private List<RoleRepresentation> getUserRoles(UserResource user) {
        return user.roles().realmLevel().listAll();
    }

    @Override
    public String getGuestOwnerByEmailAndPersist(String email) {
        return mapAndPersistGuestUserResourceToOwnerModel(getUserRepresentationByEmail(email));
    }

    public ClientModel getClientByServiceAccountUuidAndPersist(String uuid) {
        UserResource user = findUserById(uuid);
        String clientId = user.toRepresentation().getServiceAccountClientId();
        ClientResource client = findClientById(clientId);
        OwnerModel ownerModel = mapAndPersistClientResourceToOwnerModel(client, keycloakMappingService.getRoleModels(getUserRoles(user)), client.toRepresentation());
        return ownerModel.getClient();
    }

    @Override
    public ClientModel getClientByUuidAndPersist(String uuid) {
        ClientResource client = findClientById(uuid);
        OwnerModel ownerModel = mapClientResourceToOwnerModel(client);
        ownerService.save(ownerModel);
        return ownerModel.getClient();
    }

    @Override
    public Customer getCustomerInfoByEmail(String email) {
        UserRepresentation userRepresentation = getUserRepresentationByEmail(email);
        UserResource user = findUserById(userRepresentation.getId());
        return keycloakMappingService.mapUserRepresentationToCustomer(getUserRoles(user), userRepresentation);
    }

    private String mapAndPersistGuestUserResourceToOwnerModel(UserRepresentation userRepresentation) {
        OwnerModel owner = this.mapUserResourceToOwner(userRepresentation);
        ownerService.saveGuest(owner);
        return owner.getPrincipalName();
    }

    private OwnerModel mapAndPersistUserResourceToOwnerModel(UserRepresentation userRepresentation) {
        OwnerModel owner = this.mapUserResourceToOwner(userRepresentation);
        return ownerService.save(owner);
    }

    private OwnerModel saveGuestOrOwner(OwnerModel mappedOwner) {
        return mappedOwner.getRoles().isEmpty() ? ownerService.saveGuest(mappedOwner) : ownerService.save(mappedOwner);
    }

    private OwnerModel mapAndPersistClientResourceToOwnerModel(ClientResource clientResource, Collection<RoleModel> serviceAccountRoles, ClientRepresentation clientRepresentation) {
        OwnerModel owner = mapClientResourceToOwnerModel(clientResource, serviceAccountRoles, clientRepresentation);
        return ownerService.save(owner);
    }

    private OwnerModel mapClientResourceToOwnerModel(ClientResource clientResource, Collection<RoleModel> serviceAccountRoles, ClientRepresentation clientRepresentation) {
        List<RoleRepresentation> clientRoles = clientResource.roles().list();
        Optional<UserRepresentation> serviceAccountUser = KeycloakServiceUtils.getServiceAccountUser(clientResource);
        return keycloakMappingService.mapClientRepresentationToOwnerModel(null, null, clientRepresentation, clientRoles, serviceAccountUser, serviceAccountRoles);
    }

    private UserRepresentation getUserRepresentationByUsername(String username) {
        return realm.users().searchByUsername(username, true).stream()
                .collect(findExactlyOne("Found more than one or none keycloak user searching by username: {0}", username));
    }

    private UserRepresentation getUserRepresentationByEmail(String email) {
        return realm.users().search(email, true).stream()
                .collect(findExactlyOne("Found more than one or none keycloak user searching by email: {0}", email));
    }

    @Override
    public ClientModel getClientByNameAndPersist(String name) {
        ClientRepresentation clientRepresentation = findClientRepresentationByName(name);
        ClientResource client = findClientById(clientRepresentation.getId());
        OwnerModel ownerModel = mapClientResourceToOwnerModel(client);
        ownerService.save(ownerModel);
        return ownerModel.getClient();
    }

    @Override
    public Client getClientInfoByName(String name) {
        ClientRepresentation clientRepresentation = findClientRepresentationByName(name);
        ClientResource clientResource = findClientById(clientRepresentation.getId());
        List<RoleRepresentation> clientRoles = clientResource.roles().list();
        return keycloakMappingService.mapClientRepresentationToClient(clientRoles, clientRepresentation, clientResource.getServiceAccountUser());
    }

    private ClientRepresentation findClientRepresentationByName(String name) {
        return realm.clients().findByClientId(name).stream()
                .collect(findExactlyOne("Found more than one or none keycloak client searching by name: {0}", name));
    }
}
