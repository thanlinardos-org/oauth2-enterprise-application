package com.thanlinardos.resource_server.security.keycloak;

import com.thanlinardos.resource_server.model.mapped.ClientModel;
import com.thanlinardos.resource_server.model.mapped.CustomerModel;
import com.thanlinardos.spring_enterprise_library.parse.utils.ParserUtil;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.exception.KeycloakException;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.types.OperationType;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class KeycloakServiceUtils {

    private static final String KEYCLOAK = "KEYCLOAK";

    private KeycloakServiceUtils() {
    }

    @SneakyThrows
    public static <T> Response handleRequest(Function<T, Response> request, T input, OperationType operation) {
        try (Response response = request.apply(input)) {
            HttpStatusCode status = HttpStatusCode.valueOf(response.getStatus());
            if (!status.is2xxSuccessful()) {
                throw new InternalServerErrorException("Failed to " + operation + " " + input.getClass().getSimpleName()
                        + " with status " + status + " and message: " + response.getEntity());
            }
            return response;
        } catch (InternalServerErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new KeycloakException(operation, input.getClass(), e);
        }
    }

    @SneakyThrows
    public static <T> void handleRequest(Consumer<T> request, T input, OperationType operation) {
        try {
            request.accept(input);
        } catch (Exception e) {
            throw new KeycloakException(operation, input.getClass(), e);
        }
    }

    public static CustomerModel mapUserRepresentationToCustomerModel(UserRepresentation user, Integer privilegeLevel, @Nullable Long id) {
        LocalDateTime createdAt = LocalDateTime.ofEpochSecond(user.getCreatedTimestamp(), 0, ZoneOffset.UTC);
        return CustomerModel.builder() //NOSONAR (S3252)
                .id(id)
                .username(user.getUsername())
                .uuid(UUID.fromString(user.getId()))
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .mobileNumber(getMobileNumber(user))
                .enabled(user.isEnabled())
                .privilegeLevel(privilegeLevel)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .createdBy(KEYCLOAK)
                .updatedBy(KEYCLOAK)
                .build();
    }

    @Nullable
    private static String getMobileNumber(UserRepresentation user) {
        return Optional.ofNullable(user.getRawAttributes().get("mobileNumber"))
                .map(List::getFirst)
                .orElse(null);
    }

    public static ClientModel mapClientRepresentationToClientModel(ClientRepresentation client, Integer privilegeLevel, LocalDateTime createdAt, @Nullable String serviceAccountId, @Nullable Long id) {
        return ClientModel.builder() //NOSONAR (S3252)
                .id(id)
                .uuid(UUID.fromString(client.getId()))
                .serviceAccountId(ParserUtil.safeParseUUID(serviceAccountId))
                .name(client.getClientId())
                .category(Optional.ofNullable(client.getType())
                        .orElse(getClientCategory(client)))
                .privilegeLevel(privilegeLevel)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .createdBy(KEYCLOAK)
                .updatedBy(KEYCLOAK)
                .build();
    }

    private static String getClientCategory(ClientRepresentation client) {
        if (Boolean.TRUE.equals(client.isServiceAccountsEnabled())) {
            return "CLIENT_CRED";
        }
        if (Boolean.TRUE.equals(client.isDirectAccessGrantsEnabled())) {
            return "DIRECT_ACCESS";
        }
        if (Boolean.TRUE.equals(client.isImplicitFlowEnabled())) {
            return "IMPLICIT_FLOW";
        }
        if (Boolean.TRUE.equals(client.isStandardFlowEnabled())) {
            if (Boolean.TRUE.equals(client.isPublicClient())) {
                return "AUTH_CODE_PKCE";
            } else {
                return "AUTH_CODE";
            }
        }
        return "UNKNOWN";
    }

    public static Optional<UserRepresentation> getServiceAccountUser(ClientResource clientResource) {
        try {
            return Optional.of(clientResource.getServiceAccountUser());
        } catch (BadRequestException e) {
            return Optional.empty();
        }
    }
}
