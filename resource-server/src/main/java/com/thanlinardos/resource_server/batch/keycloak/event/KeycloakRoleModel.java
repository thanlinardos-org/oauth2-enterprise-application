package com.thanlinardos.resource_server.batch.keycloak.event;

import com.thanlinardos.resource_server.model.entity.keycloak.KeycloakRoleJpa;
import com.thanlinardos.spring_enterprise_library.model.mapped.base.BasicIdModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.UUID;

import static com.thanlinardos.spring_enterprise_library.spring_cloud_security.constants.SecurityCommonConstants.ROLE_PREFIX;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class KeycloakRoleModel extends BasicIdModel<KeycloakRoleJpa, KeycloakRoleModel> {

    private UUID uuid;
    private UUID adminEventId;
    private String name;
    private boolean clientRole;

    public KeycloakRoleModel(KeycloakRoleJpa entity) {
        super(entity);
        this.uuid = entity.getUuid();
        this.name = entity.getName();
        this.clientRole = entity.isClientRole();
    }

    public static KeycloakRoleModel fromRepresentation(RoleRepresentation representation) {
        return KeycloakRoleModel.builder() //NOSONAR (S3252)
                .uuid(UUID.fromString(representation.getId()))
                .name(ROLE_PREFIX + representation.getName())
                .clientRole(representation.getClientRole())
                .build();
    }

    @Override
    public KeycloakRoleJpa toEntityOnlyId() {
        return KeycloakRoleJpa.builder().id(getId()).build(); //NOSONAR (S3252)
    }

    public KeycloakRoleJpa toEntity() {
        return KeycloakRoleJpa.builder() //NOSONAR (S3252)
                .uuid(getUuid())
                .name(getName())
                .clientRole(isClientRole())
                .build();
    }

    @Override
    public KeycloakRoleModel fromEntity(KeycloakRoleJpa entity) {
        return new KeycloakRoleModel(entity);
    }
}
