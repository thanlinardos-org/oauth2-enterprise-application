import { Component, OnInit } from '@angular/core';
import { Contact } from "src/app/model/contact.model";
import { NgForm, FormsModule } from '@angular/forms';
import { DashboardService } from 'src/app/services/dashboard/dashboard.service';
import { HeaderComponent } from '../header/header.component';
import { NgIf } from '@angular/common';


@Component({
    selector: 'app-contact',
    templateUrl: './contact.component.html',
    styleUrls: ['./contact.component.css'],
    imports: [HeaderComponent, NgIf, FormsModule]
})
export class ContactComponent implements OnInit {
  model = new Contact();

  constructor(private readonly dashboardService: DashboardService) {

  }

  ngOnInit() {
    // do nothing
  }

  saveMessage(contactForm: NgForm) {
    this.dashboardService.saveMessage(this.model).subscribe(
      responseData => {
        this.model = <any> responseData.body;
        contactForm.resetForm();
      });

  }

}
