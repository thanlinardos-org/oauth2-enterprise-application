import {Component} from '@angular/core';
import {NgForm, FormsModule} from '@angular/forms';
import {LoginService} from 'src/app/services/login/login.service';
import {RegisterDetails} from "../../model/registerDetails.model";
import {HeaderComponent} from '../header/header.component';


@Component({
    selector: 'app-login',
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.css'],
    imports: [HeaderComponent, FormsModule]
})
export class RegisterComponent {
    model = new RegisterDetails();
    showPassword = false;
    confirmedPassword = '';
    receivedEmail: string | undefined;

    constructor(private readonly loginService: LoginService) {
    }

    registerUser(registerForm: NgForm) {
        if (this.model.password !== this.confirmedPassword) {
            return;
        }
        this.loginService.registerUser(this.model).subscribe(
            responseData => {
                this.receivedEmail = responseData.headers.get('Location')?.split('/').pop();
                registerForm.resetForm();
            });
    }
}
