import {ActivatedRouteSnapshot, CanActivateFn, RouterStateSnapshot, UrlTree} from '@angular/router';
import {User} from '../model/user.model';
import {AuthGuardData, createAuthGuard} from "keycloak-angular";
import Keycloak, { KeycloakProfile } from "keycloak-js";

function getMobileNumber(userProfile: KeycloakProfile) {
    return (userProfile.attributes?.['mobileNumber'] as string[])?.at(0) ?? '';
}

const loadAndStoreUserProfile = async (keycloak: Keycloak, realmRoles: string[]): Promise<void> => {
    const userProfile = await keycloak.loadUserProfile();
    const user = new User();
    user.uuid = userProfile.id ?? '';
    user.authDetails.authStatus = 'AUTH';
    user.authDetails.roles = realmRoles;
    user.name = userProfile.username ?? '';
    user.details.email = userProfile.email ?? '';
    user.details.firstName = userProfile.firstName ?? '';
    user.details.lastName = userProfile.lastName ?? '';
    user.details.mobileNumber = getMobileNumber(userProfile);
    globalThis.sessionStorage.setItem('userdetails', JSON.stringify(user));
}

const isAccessAllowed = async (route: ActivatedRouteSnapshot, state: RouterStateSnapshot, authData: AuthGuardData): Promise<boolean | UrlTree> => {
    const {authenticated, grantedRoles, keycloak} = authData;
    const roles = grantedRoles.realmRoles;

    if (authenticated) {
        await loadAndStoreUserProfile(keycloak, roles);
    } else {
        await keycloak.login({
            redirectUri: globalThis.location.origin + state.url,
        });
    }

    const requiredRoles = route.data['roles'];
    if (!requiredRoles || !(Array.isArray(requiredRoles)) || requiredRoles.length === 0) {
        return true;
    }
    return requiredRoles.some((role) => roles.includes(role));
};

export const canActivateAuthRole = createAuthGuard<CanActivateFn>(isAccessAllowed);
