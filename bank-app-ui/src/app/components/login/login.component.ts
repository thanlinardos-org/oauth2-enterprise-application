import {Component, OnInit} from '@angular/core';
import {User} from "src/app/model/user.model";
import { NgForm, FormsModule } from '@angular/forms';
import {LoginService} from 'src/app/services/login/login.service';
import {Router} from '@angular/router';
import {getCookie} from "typescript-cookie";
import { HeaderComponent } from '../header/header.component';
import { NgIf } from '@angular/common';


@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css'],
    imports: [HeaderComponent, FormsModule, NgIf]
})
export class LoginComponent implements OnInit {
    model = new User();

    constructor(private readonly loginService: LoginService, private readonly router: Router) {

    }

    ngOnInit(): void {
        // do nothing
    }

    // only works for BASIC authentication
    validateUser(loginForm: NgForm) {
        this.loginService.validateLoginDetails(this.model).subscribe(
            responseData => {
                window.sessionStorage.setItem("Authorization", responseData.headers.get('Authorization')!);
                this.model = <any>responseData.body;

                this.model.authDetails.authStatus = 'AUTH';
                window.sessionStorage.setItem("userdetails", JSON.stringify(this.model));
                let xsrf = getCookie("XSRF-TOKEN")!;
                window.sessionStorage.setItem("xsrf", xsrf);

                if (this.model.authDetails.roles.some(role => role === 'ROLE_USER')) {
                    this.router.navigate(['dashboard']);
                } else {
                    this.router.navigate(['home']);
                }
            });

    }

}
