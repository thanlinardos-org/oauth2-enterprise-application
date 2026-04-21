package com.thanlinardos.resource_server.service;

import com.thanlinardos.resource_server.model.entity.owner.ClientJpa;
import com.thanlinardos.resource_server.model.entity.owner.OwnerJpa;
import com.thanlinardos.resource_server.model.info.Client;
import com.thanlinardos.resource_server.model.mapped.ClientModel;
import com.thanlinardos.resource_server.repository.api.ClientRepository;
import com.thanlinardos.resource_server.service.owner.ClientService;
import com.thanlinardos.resource_server.service.user.api.UserService;
import com.thanlinardos.spring_enterprise_library.annotations.CoreTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@CoreTest
class ClientServiceTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 1, 1, 1, 1);

    @Mock private UserService userService;
    @Mock private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @BeforeEach
    void setup() {
        clientService = new ClientService(clientRepository, userService);
    }

    public static Stream<Arguments> getClientsParams() {
        return Stream.of(
                Arguments.of("empty list", Collections.emptyList()),
                Arguments.of("has clients", List.of(
                        ClientModel.builder() //NOSONAR (S3252)
                                .id(1L)
                                .name("client1")
                                .build(),
                        ClientModel.builder() //NOSONAR (S3252)
                                .id(2L)
                                .name("client2")
                                .build()
                ))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getClientsParams")
    void getClients(String description, List<ClientModel> expected) {
        when(clientRepository.findAll()).thenReturn(getEntities(expected));
        List<ClientModel> actual = clientService.getClients();
        Assertions.assertEquals(expected, actual);
    }

    private static List<ClientJpa> getEntities(List<ClientModel> expected) {
        return expected.stream()
                .map(ClientModel::toEntity)
                .toList();
    }

    @Test
    void getClientByNameOrFetch() {
        String clientName = "testClient";
        ClientJpa clientJpa = ClientJpa.builder().name(clientName).owner(OwnerJpa.builder().id(1L).build()).build(); //NOSONAR (S3252)
        when(clientRepository.getFirstByName(anyString())).thenReturn(Optional.of(clientJpa));

        ClientModel expected = new ClientModel(clientJpa);
        ClientModel actual = clientService.getClientByNameOrFetch(clientName);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void getClientInfoByNameOrFetch() {
        String clientName = "testClient";
        ClientJpa clientJpa = ClientJpa.builder().name(clientName).owner(buildOwnerWithClient(clientName)).build(); //NOSONAR (S3252)
        when(clientRepository.getFirstByName(anyString())).thenReturn(Optional.of(clientJpa));

        Client client = clientService.getClientInfoByNameOrFetch(clientName);
        Assertions.assertEquals(clientName, client.getName());
    }

    private OwnerJpa buildOwnerWithClient(String clientName) {
        ClientJpa client = ClientJpa.builder().name(clientName).build(); //NOSONAR (S3252)
        OwnerJpa owner = OwnerJpa.builder().id(1L).type("CLIENT").client(client).createdAt(CREATED_AT).build(); //NOSONAR (S3252)
        client.setOwner(owner);
        return owner;
    }
}