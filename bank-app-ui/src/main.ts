import {HttpRequest, provideHttpClient, withInterceptors} from '@angular/common/http';
import {
    AutoRefreshTokenService,
    CUSTOM_BEARER_TOKEN_INTERCEPTOR_CONFIG,
    customBearerTokenInterceptor,
    provideKeycloak,
    UserActivityService,
    withAutoRefreshToken
} from 'keycloak-angular';
import {environment} from './environments/environment';
import {bootstrapApplication, BrowserModule} from '@angular/platform-browser';
import {FormsModule} from '@angular/forms';
import {AppComponent} from './app/app.component';
import {importProvidersFrom, provideZoneChangeDetection} from '@angular/core';
import {routes} from "./app/app-routing";
import {provideRouter} from "@angular/router";

const keycloakProvider = provideKeycloak({
    config: environment.keycloak.config,
    initOptions: {
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: globalThis.location.origin + '/silent-check-sso.html',
        redirectUri: globalThis.location.origin + '/dashboard',
    },
    features: [
        withAutoRefreshToken({
            onInactivityTimeout: 'logout',
            sessionTimeout: 1200000
        })
    ],
    providers: [AutoRefreshTokenService, UserActivityService]
});

const customKeycloakBearerTokenInterceptor = {
    provide: CUSTOM_BEARER_TOKEN_INTERCEPTOR_CONFIG,
    useValue: [{
        shouldAddToken: async (req: any, _: any, keycloak: any) => environment.rootUrl && req.url.startsWith(environment.rootUrl) && keycloak.authenticated,
        shouldUpdateToken: (_: HttpRequest<any>) => false
    }]
};

bootstrapApplication(AppComponent, {
    providers: [
        provideZoneChangeDetection({eventCoalescing: true}),
        importProvidersFrom(BrowserModule, FormsModule),
        keycloakProvider,
        provideRouter(routes),
        customKeycloakBearerTokenInterceptor,
        provideHttpClient(withInterceptors([customBearerTokenInterceptor]))
    ]
}).catch(err => console.error(err));
