package com.thanlinardos.resource_server.service.owner;

import com.thanlinardos.resource_server.model.entity.owner.ClientJpa;
import com.thanlinardos.resource_server.model.info.Client;
import com.thanlinardos.resource_server.model.mapped.ClientModel;
import com.thanlinardos.resource_server.model.mapped.OwnerModel;
import com.thanlinardos.resource_server.repository.api.ClientRepository;
import com.thanlinardos.resource_server.service.user.api.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserService userService;

    public List<ClientModel> getClients() {
        return Optional.of(clientRepository.findAll().stream()
                .map(ClientModel::new)
                .toList())
                .orElseGet(() -> userService.syncAndGetAllClients(false));
    }

    public ClientModel getClientByNameOrFetch(String name) {
        return clientRepository.getFirstByName(name)
                .map(ClientModel::new)
                .orElseGet(() -> userService.getClientByNameAndPersist(name));
    }

    public Client getClientInfoByNameOrFetch(String name) {
        return clientRepository.getFirstByName(name)
                .map(ClientJpa::getOwner)
                .map(OwnerModel::new)
                .map(OwnerModel::toClientInfo)
                .orElseGet(() -> userService.getClientInfoByName(name));
    }

}
