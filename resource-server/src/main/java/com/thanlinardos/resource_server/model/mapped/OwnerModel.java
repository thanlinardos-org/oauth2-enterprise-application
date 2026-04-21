package com.thanlinardos.resource_server.model.mapped;

import com.thanlinardos.resource_server.model.entity.owner.ClientJpa;
import com.thanlinardos.resource_server.model.entity.owner.CustomerJpa;
import com.thanlinardos.resource_server.model.entity.owner.OwnerJpa;
import com.thanlinardos.resource_server.model.info.Client;
import com.thanlinardos.resource_server.model.info.Customer;
import com.thanlinardos.resource_server.model.info.OwnerDetailsInfo;
import com.thanlinardos.resource_server.model.info.OwnerType;
import com.thanlinardos.resource_server.model.mapped.base.BasicAuditableModel;
import com.thanlinardos.spring_enterprise_library.error.errorcodes.ErrorCode;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.base.PrivilegedResource;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.utils.ModelUtils;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class OwnerModel extends BasicAuditableModel<OwnerJpa> implements Serializable, PrivilegedResource {

    private final UUID uuid;
    private String principalName;
    private OwnerType type;
    private int privilegeLevel;
    @Builder.Default
    private Set<RoleModel> roles = new HashSet<>();
    @Nullable
    private CustomerModel customer;
    @Nullable
    private ClientModel client;

    public OwnerModel(OwnerJpa entity) {
        super(entity);
        this.uuid = entity.getUuid();
        this.principalName = entity.getName();
        this.privilegeLevel = entity.getPrivilegeLevel();
        this.roles = ModelUtils.getModelsSetFromEntities(entity.getRoles(), RoleModel::new);
        if (entity.getCustomer() != null) {
            this.customer = new CustomerModel(entity.getCustomer());
        } else if (entity.getClient() != null) {
            this.client = new ClientModel(entity.getClient());
        }
        this.type = OwnerType.valueOf(entity.getType());
    }

    public OwnerDetailsInfo toInfo() {
        return switch (type) {
            case CUSTOMER -> toCustomerInfo();
            case CLIENT -> toClientInfo();
        };
    }

    public Client toClientInfo() {
        if (client == null) {
            throw new IllegalArgumentException("Client is null for owner with id " + this.getId());
        }
        return Client.builder() //NOSONAR (S3252)
                .uuid(this.uuid)
                .serviceAccountId(this.client.getServiceAccountId())
                .category(this.client.getCategory())
                .name(this.client.getName())
                .createDt(this.getCreatedAt().toLocalDate())
                .roles(this.roles)
                .build();
    }

    public Customer toCustomerInfo() {
        if (customer == null) {
            throw ErrorCode.ILLEGAL_ARGUMENT.createCoreException("Customer is null for owner with id {0}", new Object[]{this.getId()});
        }
        return Customer.builder() //NOSONAR (S3252)
                .uuid(this.uuid)
                .name(this.customer.getUsername())
                .email(this.customer.getEmail())
                .mobileNumber(this.customer.getMobileNumber())
                .createDt(this.getCreatedAt().toLocalDate())
                .roles(this.roles)
                .build();
    }

    public Collection<String> getRoleNames() {
        return roles.stream()
                .map(RoleModel::getName)
                .toList();
    }

    @Override
    public OwnerJpa toEntityOnlyId() {
        return OwnerJpa.builder().id(getId()).build(); //NOSONAR (S3252)
    }

    public OwnerJpa toEntity() {
        OwnerJpa entity = OwnerJpa.builder() //NOSONAR (S3252)
                .id(getId())
                .uuid(getUuid())
                .name(getPrincipalName())
                .type(getType().toString())
                .privilegeLevel(getPrivilegeLevel())
                .roles(getRoles().stream()
                        .map(RoleModel::toEntity)
                        .toList())
                .build();
        entity.setTrackedProperties(this);
        if (getCustomer() != null) {
            CustomerJpa customerJpa = getCustomer().toEntity();
            customerJpa.setOwner(entity);
            entity.setCustomer(customerJpa);
        } else if (getClient() != null) {
            ClientJpa clientJpa = getClient().toEntity();
            clientJpa.setOwner(entity);
            entity.setClient(clientJpa);
        }
        return entity;
    }

    @Override
    public OwnerModel fromEntity(OwnerJpa entity) {
        return new OwnerModel(entity);
    }
}
