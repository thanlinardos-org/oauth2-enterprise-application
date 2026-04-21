package com.thanlinardos.resource_server.model.entity.owner;

import com.thanlinardos.resource_server.model.entity.base.BasicAuditableJpa;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.converters.UUIDConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "client")
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class ClientJpa extends BasicAuditableJpa {

    @OneToOne(mappedBy = "client", fetch = FetchType.EAGER)
    @ToString.Exclude
    private OwnerJpa owner;
    private String name;
    private String category;
    @Convert(converter = UUIDConverter.class)
    private UUID serviceAccountId;
}
