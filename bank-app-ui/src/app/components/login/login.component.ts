import {Component} from '@angular/core';
import {User} from "src/app/model/user.model";
import {NgForm, FormsModule} from '@angular/forms';
import {LoginService} from 'src/app/services/login/login.service';
import {Router} from '@angular/router';
import {getCookie} from "typescript-cookie";
import {HeaderComponent} from '../header/header.component';


@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css'],
    imports: [HeaderComponent, FormsModule]
})
export class LoginComponent {
    model = new User();

    constructor(private readonly loginService: LoginService, private readonly router: Router) {

    }

    // only works for BASIC authentication
    validateUser(loginForm: NgForm) {
        this.loginService.validateLoginDetails(this.model).subscribe(
            responseData => {
                globalThis.sessionStorage.setItem("Authorization", responseData.headers.get('Authorization')!);
                this.model = <any>responseData.body;

                this.model.authDetails.authStatus = 'AUTH';
                globalThis.sessionStorage.setItem("userdetails", JSON.stringify(this.model));
                const xsrf = getCookie("XSRF-TOKEN")!;
                globalThis.sessionStorage.setItem("xsrf", xsrf);

                if (this.model.authDetails.roles.includes('ROLE_USER')) {
                    this.router.navigate(['dashboard']);
                } else {
                    this.router.navigate(['home']);
                }
            });
    }

}
