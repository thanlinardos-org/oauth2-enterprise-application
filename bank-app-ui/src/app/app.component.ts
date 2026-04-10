import {Component, isDevMode, OnInit} from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
    imports: [RouterOutlet]
})
export class AppComponent implements OnInit {
  ngOnInit(): void {
      if (isDevMode()) {
          console.log('Development!');
      }
  }
  title = 'bank-app-ui';
}
