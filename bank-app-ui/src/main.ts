import {provideHttpClient, withInterceptors, withXsrfConfiguration} from '@angular/common/http';
import {customBearerTokenInterceptor} from 'keycloak-angular';
import {bootstrapApplication, BrowserModule} from '@angular/platform-browser';
import {FormsModule} from '@angular/forms';
import {AppComponent} from './app/app.component';
import {importProvidersFrom, provideZoneChangeDetection} from '@angular/core';
import {routes} from "./app/app-routing";
import {provideRouter} from "@angular/router";
import {customKeycloakBearerTokenInterceptor, keycloakProvider} from "./app/interceptors/app.request.interceptor";

bootstrapApplication(AppComponent, {
    providers: [
        provideZoneChangeDetection({eventCoalescing: true}),
        importProvidersFrom(BrowserModule, FormsModule),
        keycloakProvider,
        provideRouter(routes),
        customKeycloakBearerTokenInterceptor,
        provideHttpClient(
            withInterceptors([customBearerTokenInterceptor]),
            withXsrfConfiguration({})
        )
    ]
}).catch(err => console.error(err));
