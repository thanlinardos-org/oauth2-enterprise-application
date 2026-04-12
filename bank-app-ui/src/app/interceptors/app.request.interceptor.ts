import {HttpRequest} from '@angular/common/http';
import {
    AutoRefreshTokenService,
    CUSTOM_BEARER_TOKEN_INTERCEPTOR_CONFIG, provideKeycloak, UserActivityService, withAutoRefreshToken
} from "keycloak-angular";
import {environment} from "../../environments/environment";

export const keycloakProvider = provideKeycloak({
    config: environment.keycloak.config,
    initOptions: {
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: globalThis.location.origin + '/silent-check-sso.html',
        redirectUri: globalThis.location.origin + '/dashboard',
        // checkLoginIframe: false,
    },
    features: [
        withAutoRefreshToken({
            onInactivityTimeout: 'logout',
            sessionTimeout: 1200000
        })
    ],
    providers: [AutoRefreshTokenService, UserActivityService]
});

export const customKeycloakBearerTokenInterceptor = {
    provide: CUSTOM_BEARER_TOKEN_INTERCEPTOR_CONFIG,
    useValue: [{
        shouldAddToken: async (req: HttpRequest<any>, _: any, keycloak: any) => isApiRequestAndAuthenticated(req, keycloak),
        shouldUpdateToken: (_: HttpRequest<any>) => false
    }]
};

function isApiRequestAndAuthenticated(req: HttpRequest<any>, keycloak: any) {
    return environment.apiUrl && req.url.startsWith(environment.apiUrl) && keycloak.authenticated;
}
