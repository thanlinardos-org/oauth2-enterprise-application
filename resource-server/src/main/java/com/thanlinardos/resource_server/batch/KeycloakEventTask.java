package com.thanlinardos.resource_server.batch;

import com.thanlinardos.resource_server.batch.keycloak.event.KeycloakEventModel;
import com.thanlinardos.resource_server.service.keycloak.KeycloakEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@ConditionalOnExpression("'${scheduling.enabled}'=='true' && '${thanlinardos.springenterpriselibrary.oauth2.auth-server}' == 'KEYCLOAK'")
@RequiredArgsConstructor
public class KeycloakEventTask {

    private final KeycloakEventService keycloakEventService;

    @Scheduled(fixedDelay = 10000)
    public void processEvents() {
        List<KeycloakEventModel> stillFailingEvents = keycloakEventService.processFailedEvents();
        keycloakEventService.processEvents(keycloakEventService.fetchSortedKeycloakEvents(), stillFailingEvents);
    }
}
