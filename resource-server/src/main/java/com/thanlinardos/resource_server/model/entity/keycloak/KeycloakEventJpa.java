package com.thanlinardos.resource_server.model.entity.keycloak;

import com.thanlinardos.resource_server.batch.keycloak.event.EventStatusType;
import com.thanlinardos.resource_server.batch.keycloak.event.KeycloakUserEventType;
import com.thanlinardos.resource_server.model.entity.keycloak.converter.EventStatusConverter;
import com.thanlinardos.resource_server.model.entity.keycloak.converter.KeycloakEventTypeConverter;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "keycloak_event")
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class KeycloakEventJpa extends BasicIdJpa {

    @Column(name = "uuid", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID uuid;

    @Column(name = "time", nullable = false)
    private LocalDateTime time;

    @Column(name = "status", nullable = false)
    @Convert(converter = EventStatusConverter.class)
    private EventStatusType status;

    @Column(name = "realm_id", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID realmId;

    @Column(name = "error")
    private String error;

    @Column(name = "type", nullable = false)
    @Convert(converter = KeycloakEventTypeConverter.class)
    private KeycloakUserEventType type;

    @Column(name = "client_id", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID clientId;

    @Column(name = "user_id", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID userId;

    @OneToMany(mappedBy = "keycloakEvent")
    @ToString.Exclude
    @Builder.Default
    private List<KeycloakEventDetailsJpa> details = new ArrayList<>();

    public void addDetailWithLink(KeycloakEventDetailsJpa detail) {
        EntityUtils.addMemberWithLink(this, detail, detail::setKeycloakEvent, details);
    }
}
