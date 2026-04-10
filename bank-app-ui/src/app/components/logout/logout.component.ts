import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { User } from 'src/app/model/user.model';
import { HeaderComponent } from '../header/header.component';

@Component({
    selector: 'app-logout',
    templateUrl: './logout.component.html',
    styleUrls: ['./logout.component.css'],
    imports: [HeaderComponent]
})
export class LogoutComponent implements OnInit {

  user = new User();
  constructor(private readonly router : Router) {
  }

    ngOnInit(): void {
        globalThis.sessionStorage.setItem("userdetails", "");
        globalThis.sessionStorage.setItem("Authorization", "");
        globalThis.sessionStorage.setItem("XSRF-TOKEN", "");
        this.router.navigate(['/login']);
    }
}
