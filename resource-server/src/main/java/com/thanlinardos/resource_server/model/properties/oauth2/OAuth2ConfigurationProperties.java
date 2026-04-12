package com.thanlinardos.resource_server.model.properties.oauth2;

import com.thanlinardos.spring_enterprise_library.spring_cloud_security.model.types.OAuth2AuthServerType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "thanlinardos.springenterpriselibrary.oauth2")
public class OAuth2ConfigurationProperties {

    private OAuth2AuthServerType authServer;
    private OAuthClientProperties github = new OAuthClientProperties();
    private OAuthClientProperties facebook = new OAuthClientProperties();

    @Getter
    @Setter
    public static class OAuthClientProperties {

        private Client client = new Client();

        @Getter
        @Setter
        public static class Client {
            private String id;
            private String secret;
        }
    }
}

