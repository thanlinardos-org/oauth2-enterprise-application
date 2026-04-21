package com.thanlinardos.resource_server.model.entity.keycloak;

import com.thanlinardos.spring_enterprise_library.model.entity.base.BasicIdJpa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "keycloak_event_details")
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class KeycloakEventDetailsJpa extends BasicIdJpa {

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    private KeycloakEventJpa keycloakEvent;

    @Column(name = "name", nullable = false, length = 55)
    private String name;

    @Column(name = "val", length = 500)
    @Nullable
    private String val;

    public static KeycloakEventDetailsJpa fromMap(Map.Entry<String, String> entry) {
        return KeycloakEventDetailsJpa.builder() //NOSONAR (S3252)
                .name(entry.getKey())
                .val(entry.getValue())
                .build();
    }

    public static Map<String, String> toMap(List<KeycloakEventDetailsJpa> details) {
        Map<String, String> map = new HashMap<>();
        for (KeycloakEventDetailsJpa detail : details) {
            map.put(detail.getName(), detail.getVal());
        }
        return map;
    }
}
