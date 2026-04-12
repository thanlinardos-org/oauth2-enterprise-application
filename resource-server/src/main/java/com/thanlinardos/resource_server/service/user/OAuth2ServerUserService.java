package com.thanlinardos.resource_server.service.user;

import com.thanlinardos.resource_server.model.info.Client;
import com.thanlinardos.resource_server.model.info.Customer;
import com.thanlinardos.resource_server.model.info.OwnerType;
import com.thanlinardos.resource_server.model.info.RegisterCustomerDetails;
import com.thanlinardos.resource_server.model.mapped.ClientModel;
import com.thanlinardos.resource_server.model.mapped.CustomerModel;
import com.thanlinardos.resource_server.model.mapped.OwnerModel;
import com.thanlinardos.resource_server.service.user.api.UserService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public class OAuth2ServerUserService implements UserService {

    public OAuth2ServerUserService(String authServerName) {
        log.info("OAuth2ServerUserService created for authorization server: {}", authServerName);
    }

    @Override
    public List<CustomerModel> syncAndGetAllCustomers(boolean shouldUpdateExisting) {
        return List.of();
    }

    @Override
    public List<ClientModel> syncAndGetAllClients(boolean shouldUpdateExisting) {
        return List.of();
    }

    @Override
    public String createGuestCustomer(RegisterCustomerDetails customerDetails) {
        return "";
    }

    @Override
    public OwnerModel createCustomerWithRoles(Customer customer) {
        return null;
    }

    @Override
    public CustomerModel getCustomerByUsernameAndPersist(String username) {
        return null;
    }

    @Override
    public CustomerModel getCustomerByEmailAndPersist(String email) {
        return null;
    }

    @Override
    public OwnerModel getOwnerByIdAndPersistOrUpdate(UUID uuid, OwnerType ownerType) {
        return null;
    }

    @Override
    public String getGuestOwnerByEmailAndPersist(String email) {
        return null;
    }

    @Override
    public ClientModel getClientByUuidAndPersist(String uuid) {
        return null;
    }

    @Override
    public Customer getCustomerInfoByEmail(String email) {
        return null;
    }

    @Override
    public ClientModel getClientByNameAndPersist(String name) {
        return null;
    }

    @Override
    public Client getClientInfoByName(String name) {
        return null;
    }
}
