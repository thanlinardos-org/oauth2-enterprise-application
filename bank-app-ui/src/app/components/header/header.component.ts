import {Component, inject, OnInit} from '@angular/core';
import {User} from 'src/app/model/user.model';
import Keycloak, {KeycloakProfile} from "keycloak-js";
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';


@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.css'],
    imports: [RouterLink, RouterLinkActive, RouterOutlet]
})
export class HeaderComponent implements OnInit {

    user = new User();

    public isLoggedIn: boolean = false;
    public userProfile: KeycloakProfile | null = null;

    private readonly keycloak = inject(Keycloak);

    public ngOnInit() {
        void (async () => { // avoid return type SQ warning 'S6544'
            this.isLoggedIn = !this.keycloak.loginRequired;
            this.userProfile = this.keycloak.profile ?? null;

            if (this.userProfile !== null) {
                this.user.uuid = this.userProfile.id ?? '';
                this.user.name = this.userProfile.username ?? '';
                this.user.details.email = this.userProfile.email ?? '';
                this.user.details.firstName = this.userProfile.firstName ?? '';
                this.user.details.lastName = this.userProfile.lastName ?? '';
                this.user.details.mobileNumber = (this.userProfile.attributes?.['mobileNumber'] as string[])?.at(0) ?? '';
                this.user.authDetails.authStatus = 'AUTH';
                globalThis.sessionStorage.setItem('userdetails', JSON.stringify(this.user));
            }
        })();
    }

    public login() {
        this.keycloak.login({redirectUri: globalThis.location.origin + '/dashboard'})
            .catch(error => console.error(error));
    }

    public logout() {
        this.keycloak.logout({
            redirectUri: globalThis.location.origin + '/home'
        }).catch(error => console.error(error));
    }

    public register() {
        this.keycloak.register({redirectUri: globalThis.location.origin + '/dashboard'})
            .catch(error_ => console.error(error_));
    }
}
