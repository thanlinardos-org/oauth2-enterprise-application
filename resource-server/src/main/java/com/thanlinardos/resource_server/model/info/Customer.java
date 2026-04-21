package com.thanlinardos.resource_server.model.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thanlinardos.resource_server.model.mapped.RoleModel;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class Customer extends OwnerDetailsInfo implements Serializable {

    @Email
    @NotBlank
    private String email;
    @Digits(integer = 10, fraction = 0)
    private String mobileNumber;
    private String firstName;
    private String lastName;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    @NotBlank
    private String password;

    @JsonCreator
    public Customer(@Email @NotBlank String email, @NotBlank String name, @Digits(integer = 10, fraction = 0) String mobileNumber, String firstName, String lastName, Set<String> roles, @NotBlank String password) {
        setName(name);
        setRoles(getRoleModels(roles));
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
    }

    private Set<RoleModel> getRoleModels(Set<String> roleNames) {
        return roleNames.stream()
                .map(role -> RoleModel.builder().role(role).build()) //NOSONAR (S3252)
                .collect(Collectors.toSet());
    }

    public RegisterCustomerDetails toRegisterCustomerDetails() {
        return new RegisterCustomerDetails(email, mobileNumber, firstName, lastName, password);
    }

    @Override
    public String getPrincipalName() {
        return email;
    }
}
