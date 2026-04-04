import { Component, OnInit } from '@angular/core';
import { User } from 'src/app/model/user.model';
import { DashboardService } from '../../services/dashboard/dashboard.service';
import {AccountTransactions} from "../../model/account.transactions.model";
import { HeaderComponent } from '../header/header.component';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';


@Component({
    selector: 'app-balance',
    templateUrl: './balance.component.html',
    styleUrls: ['./balance.component.css'],
    imports: [HeaderComponent, RouterLink, CurrencyPipe]
})
export class BalanceComponent implements OnInit {

  user = new User();
  transactions: AccountTransactions[] = [];

  constructor(private readonly dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.user = JSON.parse(sessionStorage.getItem('userdetails') ?? "");
    if(this.user){
      this.dashboardService.getAccountTransactions(this.user.details.email).subscribe(
        responseData => {
        this.transactions = <any> responseData.body;
        });
    }
  }

}
