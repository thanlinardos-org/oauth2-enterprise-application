import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {provideKeycloak} from 'keycloak-angular';
import {environment} from './environments/environment';
import {BrowserModule, bootstrapApplication} from '@angular/platform-browser';
import {FormsModule} from '@angular/forms';
import {AppComponent} from './app/app.component';
import {importProvidersFrom} from '@angular/core';
import {provideRouter} from "@angular/router";
import {routes} from "./app/app-routing";

const httpClientProvider = provideHttpClient(withInterceptorsFromDi());
const keycloakProvider = provideKeycloak({
    config: environment.keycloak.config,
    initOptions: {
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: globalThis.location.origin + '/silent-check-sso.html',
        redirectUri: globalThis.location.origin + '/dashboard',
    }
});


bootstrapApplication(AppComponent, {
    providers: [
        importProvidersFrom(BrowserModule, FormsModule),
        httpClientProvider, keycloakProvider,
        provideRouter(routes)
    ]
}).catch(err => console.error(err));
