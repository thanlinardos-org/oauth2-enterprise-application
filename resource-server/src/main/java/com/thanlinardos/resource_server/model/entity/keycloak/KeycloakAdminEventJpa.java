package com.thanlinardos.resource_server.model.entity.keycloak;

import com.thanlinardos.resource_server.batch.keycloak.event.AdminEventOperationType;
import com.thanlinardos.resource_server.batch.keycloak.event.AdminEventResourceType;
import com.thanlinardos.resource_server.batch.keycloak.event.EventStatusType;
import com.thanlinardos.resource_server.model.entity.keycloak.converter.AdminEventOperationTypeConverter;
import com.thanlinardos.resource_server.model.entity.keycloak.converter.AdminEventResourceTypeConverter;
import com.thanlinardos.resource_server.model.entity.keycloak.converter.EventStatusConverter;
import com.thanlinardos.spring_enterprise_library.model.entity.base.BasicIdJpa;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.converters.UUIDConverter;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.utils.EntityUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "keycloak_admin_event")
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class KeycloakAdminEventJpa extends BasicIdJpa {

    @Column(name = "uuid", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID uuid;

    @Column(name = "time", nullable = false)
    private LocalDateTime time;

    @Column(name = "status", nullable = false, length = 55)
    @Convert(converter = EventStatusConverter.class)
    private EventStatusType status;

    @Column(name = "realm_id", nullable = false, length = 37)
    @Convert(converter = UUIDConverter.class)
    private UUID realmId;

    @Column(name = "error", length = 500)
    @Nullable
    private String error;

    @Column(name = "client_id", nullable = false, length = 37)
    @Convert(converter = UUIDConverter.class)
    @Nullable
    private UUID clientId;

    @Column(name = "user_id", nullable = false, length = 37)
    @Convert(converter = UUIDConverter.class)
    @Nullable
    private UUID userId;

    @Column(name = "operation_type", nullable = false, length = 55)
    @Convert(converter = AdminEventOperationTypeConverter.class)
    private AdminEventOperationType operationType;

    @Column(name = "resource_type", nullable = false, length = 55)
    @Convert(converter = AdminEventResourceTypeConverter.class)
    private AdminEventResourceType resourceType;

    @Column(name = "resource_path", nullable = false, length = 2000)
    private String resourcePath;

    @OneToMany(mappedBy = "keycloakAdminEvent")
    @ToString.Exclude
    @Builder.Default
    private List<KeycloakRoleJpa> roles = new ArrayList<>();

    public void addRoleWithLink(KeycloakRoleJpa role) {
        EntityUtils.addMemberWithLink(this, role, role::setKeycloakAdminEvent, roles);
    }
}
