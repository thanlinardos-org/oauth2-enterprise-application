package com.thanlinardos.resource_server.security.keycloak;

import com.thanlinardos.resource_server.model.properties.keycloak.KeycloakClientProperties;
import com.thanlinardos.resource_server.model.properties.keycloak.KeycloakConfigurationProperties;
import com.thanlinardos.resource_server.model.properties.keycloak.KeycloakProperties;
import com.thanlinardos.spring_enterprise_library.https.SecureHttpRequestFactory;
import com.thanlinardos.spring_enterprise_library.https.properties.KeyAndTrustStoreProperties;
import com.thanlinardos.spring_enterprise_library.https.utils.SslContextUtil;
import jakarta.ws.rs.client.ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@Configuration
@ConditionalOnExpression("'${integration.test.enabled}'=='false' && '${thanlinardos.springenterpriselibrary.oauth2.auth-server}' == 'KEYCLOAK'")
@EnableConfigurationProperties(KeycloakConfigurationProperties.class)
@Slf4j
public class KeycloakAdminConfig {

    @Bean
    @RefreshScope
    public KeycloakProperties keycloakProperties(KeycloakConfigurationProperties props) {
        KeycloakConfigurationProperties.Client client = props.getClient();
        KeyAndTrustStoreProperties keystore = new KeyAndTrustStoreProperties(client.getKeystore().getPath(), client.getKeystore().getPassword());
        KeyAndTrustStoreProperties truststore = new KeyAndTrustStoreProperties(client.getTruststore().getPath(), client.getTruststore().getPassword());
        KeycloakClientProperties clientProperties = new KeycloakClientProperties(client.getId(), client.getSecret(), keystore, truststore);
        return new KeycloakProperties(props.getUrl(), props.getRealm(), clientProperties);
    }

    @Bean
    @RefreshScope
    Keycloak keycloak(KeycloakProperties keycloakProperties) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException, UnrecoverableKeyException {
        log.info("Building Keycloak admin client");
        KeycloakClientProperties clientProperties = keycloakProperties.getClient();
        SSLContext sslContext = SslContextUtil.buildSSLContext(clientProperties.keystore(), clientProperties.truststore());
        return KeycloakBuilder.builder() //NOSONAR (S3252)
                .serverUrl(keycloakProperties.getUrl())
                .realm(keycloakProperties.getRealm())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientProperties.id())
                .clientSecret(clientProperties.secret())
                .resteasyClient(ClientBuilder.newBuilder()
                        .sslContext(sslContext)
                        .build())
                .build();
    }

    @Bean
    @RefreshScope
    RealmResource keycloakRealm(KeycloakProperties keycloakProperties, Keycloak keycloak) {
        log.info("Building Keycloak realm client with name: {}", keycloakProperties.getRealm());
        return keycloak.realm(keycloakProperties.getRealm());
    }

    @Bean
    @RefreshScope
    public JwtDecoder jwtDecoder(OAuth2ResourceServerProperties oAuth2ResourceServerProperties,
                                 KeycloakProperties keycloakProperties
    ) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException {
        KeycloakClientProperties clientProperties = keycloakProperties.getClient();
        SSLContext sslContext = SslContextUtil.buildSSLContext(clientProperties.keystore(), clientProperties.truststore());
        SecureHttpRequestFactory requestFactory = new SecureHttpRequestFactory(sslContext);
        return NimbusJwtDecoder
                .withIssuerLocation(oAuth2ResourceServerProperties.getJwt().getIssuerUri())
                .restOperations(new RestTemplate(requestFactory))
                .build();
    }
}
