package com.thanlinardos.resource_server.model.entity.owner;

import com.thanlinardos.resource_server.model.entity.base.BasicAuditableJpa;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "customer")
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class CustomerJpa extends BasicAuditableJpa {

    @OneToOne(mappedBy = "customer")
    @ToString.Exclude
    private OwnerJpa owner;
    private String username;
    private String email;
    private String mobileNumber;
    private String firstName;
    private String lastName;
    @Default
    private Boolean enabled = true;
    @Default
    private Boolean accountNonExpired = true;
    @Default
    private Boolean accountNonLocked = true;
    @Default
    private Boolean credentialsNonExpired = true;
}
