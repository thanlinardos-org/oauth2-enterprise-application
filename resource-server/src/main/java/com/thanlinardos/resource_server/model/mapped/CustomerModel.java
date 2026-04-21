package com.thanlinardos.resource_server.model.mapped;

import com.thanlinardos.resource_server.model.entity.owner.CustomerJpa;
import com.thanlinardos.resource_server.model.entity.owner.OwnerJpa;
import com.thanlinardos.resource_server.model.mapped.base.BasicAuditableModel;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.base.PrivilegedResource;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class CustomerModel extends BasicAuditableModel<CustomerJpa> implements Serializable, PrivilegedResource {

    private UUID uuid;
    private String username;
    private String email;
    private String mobileNumber;
    private String firstName;
    private String lastName;
    @Builder.Default
    private Boolean enabled = true;
    @Builder.Default
    private Boolean accountNonExpired = true;
    @Builder.Default
    private Boolean accountNonLocked = true;
    @Builder.Default
    private Boolean credentialsNonExpired = true;
    private int privilegeLevel;
    private long ownerId;

    public CustomerModel(CustomerJpa entity) {
        super(entity);
        setUuid(entity.getOwner().getUuid());
        setUsername(entity.getUsername());
        setEmail(entity.getEmail());
        setMobileNumber(entity.getMobileNumber());
        setFirstName(entity.getFirstName());
        setLastName(entity.getLastName());
        setEnabled(entity.getEnabled());
        setAccountNonExpired(entity.getAccountNonExpired());
        setAccountNonLocked(entity.getAccountNonLocked());
        setCredentialsNonExpired(entity.getCredentialsNonExpired());
        setPrivilegeLevel(entity.getOwner().getPrivilegeLevel());
        setOwnerId(entity.getOwner().getId());
    }

    @Override
    public String getPrincipalName() {
        return email;
    }

    @Override
    public CustomerJpa toEntityOnlyId() {
        return CustomerJpa.builder().id(getId()).build(); //NOSONAR (S3252)
    }

    public CustomerJpa toEntity() {
        CustomerJpa entity = CustomerJpa.builder() //NOSONAR (S3252)
                .id(getId())
                .username(getUsername())
                .email(getEmail())
                .firstName(getFirstName())
                .lastName(getLastName())
                .owner(OwnerJpa.builder() //NOSONAR (S3252)
                        .id(getOwnerId())
                        .build())
                .mobileNumber(getMobileNumber())
                .enabled(getEnabled())
                .accountNonExpired(getAccountNonExpired())
                .accountNonLocked(getAccountNonLocked())
                .credentialsNonExpired(getCredentialsNonExpired())
                .build();
        entity.setTrackedProperties(this);
        return entity;
    }

    @Override
    public CustomerModel fromEntity(CustomerJpa entity) {
        return new CustomerModel(entity);
    }
}
