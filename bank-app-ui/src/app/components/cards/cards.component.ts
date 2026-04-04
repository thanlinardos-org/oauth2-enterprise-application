import {Component, OnInit} from '@angular/core';
import {Cards} from 'src/app/model/cards.model';
import {User} from 'src/app/model/user.model';
import {DashboardService} from '../../services/dashboard/dashboard.service';
import {HeaderComponent} from '../header/header.component';
import { CurrencyPipe } from '@angular/common';
import {RouterLink} from '@angular/router';


@Component({
    selector: 'app-cards',
    templateUrl: './cards.component.html',
    styleUrls: ['./cards.component.css'],
    imports: [HeaderComponent, RouterLink, CurrencyPipe]
})
export class CardsComponent implements OnInit {

    user = new User();
    cards: Cards[] = [];
    currOutstandingAmt: number = 0;

    constructor(private readonly dashboardService: DashboardService) {
    }

    ngOnInit(): void {
        this.user = JSON.parse(sessionStorage.getItem('userdetails') ?? "");
        if (this.user) {
            this.dashboardService.getCardsDetails(this.user.details.email).subscribe(
                responseData => {
                    this.cards = <any>responseData.body;
                    this.cards.forEach(function (this: CardsComponent, card: Cards) {
                        this.currOutstandingAmt = this.currOutstandingAmt + card.availableAmount;
                    }.bind(this));
                });
        }
    }

}
