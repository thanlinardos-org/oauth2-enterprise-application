import {Component, OnInit} from '@angular/core';
import {DashboardService} from '../../services/dashboard/dashboard.service';
import {User} from 'src/app/model/user.model';
import {Account} from 'src/app/model/account.model';
import { HeaderComponent } from '../header/header.component';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'app-account',
    templateUrl: './account.component.html',
    styleUrls: ['./account.component.css'],
    imports: [HeaderComponent, RouterLink]
})
export class AccountComponent implements OnInit {
    user = new User();
    account = new Account();

    constructor(private readonly dashboardService: DashboardService) {
    }

    ngOnInit(): void {
        this.user = JSON.parse(sessionStorage.getItem('userdetails')!);
        if (this.user) {
            this.dashboardService.getAccountDetails(this.user.details.email).subscribe(
                responseData => {
                    this.account = <any>responseData.body;
                });
        }

    }

}
