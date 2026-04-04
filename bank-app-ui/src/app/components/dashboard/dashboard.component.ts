import {Component, OnInit} from '@angular/core';
import {User} from 'src/app/model/user.model';
import { HeaderComponent } from '../header/header.component';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'app-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css'],
    imports: [HeaderComponent, RouterLink]
})
export class DashboardComponent implements OnInit {

    user = new User();

    constructor() {

    }

    ngOnInit() {
        if (sessionStorage.getItem('userdetails')) {
            this.user = JSON.parse(sessionStorage.getItem('userdetails') ?? "");
        }
    }

}
