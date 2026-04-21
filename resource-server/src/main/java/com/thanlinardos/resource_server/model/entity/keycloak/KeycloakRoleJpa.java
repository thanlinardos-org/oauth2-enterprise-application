package com.thanlinardos.resource_server.model.entity.keycloak;

import com.thanlinardos.spring_enterprise_library.model.entity.base.BasicIdJpa;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.converters.UUIDConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "keycloak_role")
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class KeycloakRoleJpa extends BasicIdJpa {

    @Column(name = "uuid", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private UUID uuid;

    @ManyToOne
    @JoinColumn(name = "admin_event_id", nullable = false)
    @ToString.Exclude
    private KeycloakAdminEventJpa keycloakAdminEvent;

    @Column(name = "name", nullable = false, length = 55)
    private String name;

    @Column(name = "client_role", nullable = false)
    private boolean clientRole;
}
