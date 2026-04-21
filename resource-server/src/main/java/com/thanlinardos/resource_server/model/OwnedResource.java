package com.thanlinardos.resource_server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thanlinardos.resource_server.model.mapped.OwnerModel;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.base.PrivilegedResource;
import lombok.ToString;

public interface OwnedResource<T extends OwnedResource<T>> extends PrivilegedResource {

    OwnerModel getOwner();

    void setOwner(OwnerModel owner);

    default T owner(OwnerModel owner) {
        setOwner(owner);
        return (T) this;
    }

    default int getPrivilegeLevel() {
        return getOwner().getPrivilegeLevel();
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ToString.Include
    default String getPrincipalName() {
        return getOwner().getPrincipalName();
    }
}
