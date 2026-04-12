package com.thanlinardos.resource_server.model.properties.keycloak;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@Getter
@Setter
@ConfigurationProperties(prefix = "thanlinardos.springenterpriselibrary.oauth2.keycloak")
public class KeycloakConfigurationProperties {
    private String url;
    private String realm;
    private Client client = new Client();

    @Getter
    @Setter
    public static class Client {
        private String id;
        private String secret;
        private StoreProperties keystore = new StoreProperties();
        private StoreProperties truststore = new StoreProperties();
    }

    @Getter
    @Setter
    public static class StoreProperties {
        private Resource path;
        private String password;
    }
}

