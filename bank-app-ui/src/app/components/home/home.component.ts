import { Component, OnInit } from '@angular/core';
import { HeaderComponent } from '../header/header.component';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css'],
    imports: [HeaderComponent]
})
export class HomeComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
      // do nothing
  }

}
