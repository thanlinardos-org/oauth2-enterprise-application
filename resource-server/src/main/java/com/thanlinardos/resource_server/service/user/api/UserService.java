package com.thanlinardos.resource_server.service.user.api;

import com.thanlinardos.resource_server.model.info.Client;
import com.thanlinardos.resource_server.model.info.Customer;
import com.thanlinardos.resource_server.model.info.OwnerType;
import com.thanlinardos.resource_server.model.info.RegisterCustomerDetails;
import com.thanlinardos.resource_server.model.mapped.ClientModel;
import com.thanlinardos.resource_server.model.mapped.CustomerModel;
import com.thanlinardos.resource_server.model.mapped.OwnerModel;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<CustomerModel> syncAndGetAllCustomers(boolean shouldUpdateExisting);

    List<ClientModel> syncAndGetAllClients(boolean shouldUpdateExisting);

    String createGuestCustomer(RegisterCustomerDetails customerDetails);

    @Transactional
    OwnerModel createCustomerWithRoles(Customer customer);

    CustomerModel getCustomerByUsernameAndPersist(String username);

    CustomerModel getCustomerByEmailAndPersist(String email);

    OwnerModel getOwnerByIdAndPersistOrUpdate(UUID uuid, OwnerType ownerType);

    String getGuestOwnerByEmailAndPersist(String email);

    ClientModel getClientByUuidAndPersist(String uuid);

    Customer getCustomerInfoByEmail(String email);

    ClientModel getClientByNameAndPersist(String name);

    Client getClientInfoByName(String name);
}
