package com.thanlinardos.resource_server.security;

import com.thanlinardos.resource_server.model.properties.oauth2.OAuth2ConfigurationProperties;
import com.thanlinardos.resource_server.service.keycloak.KeycloakMappingService;
import com.thanlinardos.resource_server.service.owner.OwnerService;
import com.thanlinardos.resource_server.service.user.KeycloakUserService;
import com.thanlinardos.resource_server.service.user.OAuth2ServerUserService;
import com.thanlinardos.resource_server.service.user.api.UserService;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.stereotype.Component;

import java.net.URI;

import static com.thanlinardos.spring_enterprise_library.spring_cloud_security.constants.SecurityCommonConstants.ROLE_PREFIX;

@Component
@EnableConfigurationProperties(OAuth2ConfigurationProperties.class)
@RequiredArgsConstructor
public class SecurityConfigBeans {

    private final OAuth2ConfigurationProperties oauth2Properties;
    private final OAuth2ResourceServerProperties resourceServerProperties;

    @Bean
    @RefreshScope
    public UserService userService(OwnerService ownerService, RealmResource keycloakRealm, KeycloakMappingService keycloakMappingService) {
        switch (oauth2Properties.getAuthServer()) {
            case KEYCLOAK -> {
                return new KeycloakUserService(ownerService, keycloakRealm, keycloakMappingService);
            }
            case SPRING_OAUTH2_SERVER -> {
                return new OAuth2ServerUserService(URI.create(resourceServerProperties.getJwt().getIssuerUri()).getHost());
            }
            default -> throw new IllegalArgumentException("Unsupported auth server type: " + oauth2Properties.getAuthServer());
        }
    }

    // Without this the default implementation of sessionDestroyed is NO-OP and therefore would
    // not publish a HttpSessionDestroyedEvent to notify the SessionRegistryImpl#onApplicationEvent
    // that the session has been evicted.
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    static GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(ROLE_PREFIX);   // this is the default role prefix
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration github = githubClientRegistration();
        ClientRegistration facebook = facebookClientRegistration();
        return new InMemoryClientRegistrationRepository(github, facebook);
    }

    private ClientRegistration githubClientRegistration() {
        return CommonOAuth2Provider.GITHUB.getBuilder("github")
                .clientId(oauth2Properties.getGithub().getClient().getId())
                .clientSecret(oauth2Properties.getGithub().getClient().getSecret())
                .build();
    }

    private ClientRegistration facebookClientRegistration() {
        return CommonOAuth2Provider.FACEBOOK.getBuilder("facebook")
                .clientId(oauth2Properties.getFacebook().getClient().getId())
                .clientSecret(oauth2Properties.getFacebook().getClient().getSecret())
                .build();
    }
}
