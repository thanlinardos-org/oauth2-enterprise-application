package com.thanlinardos.resource_server.model.entity.owner;

import com.thanlinardos.resource_server.model.entity.account.AccountJpa;
import com.thanlinardos.resource_server.model.entity.base.BasicAuditableJpa;
import com.thanlinardos.resource_server.model.entity.role.RoleJpa;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.converters.UUIDConverter;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "owner")
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class OwnerJpa extends BasicAuditableJpa {

    @Convert(converter = UUIDConverter.class)
    private UUID uuid;
    private String name;
    private String type;
    private int privilegeLevel;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH},
            fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    @Nullable
    private CustomerJpa customer;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH},
            fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    @Nullable
    private ClientJpa client;

    @OneToOne(mappedBy = "owner")
    @ToString.Exclude
    private AccountJpa account;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "owner_roles",
            joinColumns = @JoinColumn(name = "owner_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private List<RoleJpa> roles = new ArrayList<>();

    public void setCustomerWithLink(CustomerJpa customer) {
        this.customer = customer;
        customer.setOwner(this);
    }

    public void setClientWithLink(ClientJpa client) {
        this.client = client;
        client.setOwner(this);
    }

    public void setCustomerId(long customerId) {
        Objects.requireNonNull(this.customer).setId(customerId);
    }

    public void setClientId(long clientId) {
        Objects.requireNonNull(this.client).setId(clientId);
    }
}
