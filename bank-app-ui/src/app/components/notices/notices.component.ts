import { Component, OnInit } from '@angular/core';
import { DashboardService } from 'src/app/services/dashboard/dashboard.service';
import { HeaderComponent } from '../header/header.component';

import { RouterLink } from '@angular/router';

@Component({
    selector: 'app-notices',
    templateUrl: './notices.component.html',
    styleUrls: ['./notices.component.css'],
    imports: [HeaderComponent, RouterLink]
})
export class NoticesComponent implements OnInit {

  notices: any = [];

  constructor(private readonly dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.dashboardService.getNoticeDetails().subscribe(
      responseData => {
      this.notices = responseData.body;
      });
  }

}
