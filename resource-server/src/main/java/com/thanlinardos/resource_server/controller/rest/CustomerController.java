package com.thanlinardos.resource_server.controller.rest;

import com.thanlinardos.resource_server.model.info.Client;
import com.thanlinardos.resource_server.model.info.Customer;
import com.thanlinardos.resource_server.model.info.OwnerDetailsInfo;
import com.thanlinardos.resource_server.model.info.RegisterCustomerDetails;
import com.thanlinardos.resource_server.model.mapped.ClientModel;
import com.thanlinardos.resource_server.model.mapped.CustomerModel;
import com.thanlinardos.resource_server.model.mapped.OwnerModel;
import com.thanlinardos.resource_server.service.owner.ClientService;
import com.thanlinardos.resource_server.service.owner.CustomerService;
import com.thanlinardos.resource_server.service.owner.OwnerService;
import com.thanlinardos.resource_server.service.user.api.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final OwnerService ownerService;
    private final UserService userService;
    private final ClientService clientService;

    @GetMapping("/customers/{email}")
    public ResponseEntity<CustomerModel> getCustomerByUsernameOrEmail(@PathVariable String email) {
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerModel>> getCustomers() {
        return ResponseEntity.ok(customerService.getCustomers());
    }

    @PostMapping("/sync-customers")
    public ResponseEntity<List<CustomerModel>> syncCustomers(@RequestParam boolean force) {
        return ResponseEntity.ok(userService.syncAndGetAllCustomers(force));
    }

    @PostMapping("/sync-clients")
    public ResponseEntity<List<ClientModel>> syncClients(@RequestParam boolean force) {
        return ResponseEntity.ok(userService.syncAndGetAllClients(force));
    }

    @GetMapping("/clients")
    public ResponseEntity<List<ClientModel>> getClients() {
        return ResponseEntity.ok(clientService.getClients());
    }

    @GetMapping("/clients/{name}")
    public ResponseEntity<ClientModel> getClientByName(@PathVariable String name) {
        return ResponseEntity.ok(clientService.getClientByNameOrFetch(name));
    }

    @GetMapping("/user")
    public ResponseEntity<@Valid Customer> getUserDetailsAfterLogin(Authentication authentication) {
        return ownerService.getOwnerByPrincipalName(authentication.getName())
                .map(OwnerModel::toCustomerInfo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client")
    public ResponseEntity<@Valid Client> getClientDetailsAfterLogin(Authentication authentication) {
        return ownerService.getOwnerByPrincipalName(authentication.getName())
                .map(OwnerModel::toClientInfo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner")
    public ResponseEntity<@Valid OwnerDetailsInfo> getAuthenticatedOwnerDetailsAfterLogin(Authentication authentication) {
        return ownerService.getOwnerByPrincipalName(authentication.getName())
                .map(OwnerModel::toInfo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/customers")
    public ResponseEntity<Void> registerCustomer(@Valid @RequestBody RegisterCustomerDetails customerDetails) {
        return ResponseEntity.created(URI.create("/customers/" + userService.createGuestCustomer(customerDetails)))
                .build();
    }

    @PostMapping("/user")
    public ResponseEntity<Customer> registerCustomerWithRoles(@Valid @RequestBody Customer customer) {
        return ResponseEntity.ok(userService.createCustomerWithRoles(customer).toCustomerInfo());
    }
}
