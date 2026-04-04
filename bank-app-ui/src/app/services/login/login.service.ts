import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {User} from "src/app/model/user.model";
import {AppConstants} from 'src/app/constants/app.constants';
import {environment} from '../../../environments/environment';
import {RegisterDetails} from "../../model/registerDetails.model";

@Injectable({
    providedIn: 'root'
})
export class LoginService {

    constructor(private readonly http: HttpClient) {
    }

    validateLoginDetails(user: User) {
        globalThis.sessionStorage.setItem("userdetails", JSON.stringify(user));
        return this.http.get(environment.rooturl + AppConstants.LOGIN_API_URL, {
            observe: 'response',
            withCredentials: true
        });
    }

    registerUser(registerDetails: RegisterDetails) {
        return this.http.post(environment.rooturl + AppConstants.REGISTER_API_URL, registerDetails, {
            observe: 'response',
            withCredentials: true
        });
    }
}
