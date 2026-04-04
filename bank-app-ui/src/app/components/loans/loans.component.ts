import { Component, OnInit } from '@angular/core';
import { Loans } from 'src/app/model/loans.model';
import { User } from 'src/app/model/user.model';
import { DashboardService } from '../../services/dashboard/dashboard.service';
import { HeaderComponent } from '../header/header.component';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'app-loans',
    templateUrl: './loans.component.html',
    styleUrls: ['./loans.component.css'],
    imports: [HeaderComponent, RouterLink, CurrencyPipe]
})
export class LoansComponent implements OnInit {

  user = new User();
  loans: Loans[] = [];
  currOutstandingBalance: number = 0;

  constructor(private readonly dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.user = JSON.parse(sessionStorage.getItem('userdetails') ?? "");
    if(this.user){
      this.dashboardService.getLoansDetails(this.user.details.email).subscribe(
        responseData => {
        this.loans = <any> responseData.body;
        this.loans.forEach(function (this: LoansComponent, loan: Loans) {
          this.currOutstandingBalance = this.currOutstandingBalance+loan.outstandingAmount;
        }.bind(this));
        });
    }
  }



}
