import { Routes } from '@angular/router';
import { ContactComponent } from './components/contact/contact.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AccountComponent } from './components/account/account.component';
import { BalanceComponent } from './components/balance/balance.component';
import { NoticesComponent } from './components/notices/notices.component';
import { LoansComponent } from './components/loans/loans.component';
import { CardsComponent } from './components/cards/cards.component';
import { canActivateAuthRole } from './routeguards/auth.routeguard';
import { HomeComponent } from './components/home/home.component';

export const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full'},
  { path: 'home', component: HomeComponent},
  { path: 'contact', component: ContactComponent},
  { path: 'notices', component: NoticesComponent},
  { path: 'dashboard', component: DashboardComponent, canActivate: [canActivateAuthRole], data: { roles: ['USER', 'GUEST']}},
  { path: 'myAccount', component: AccountComponent, canActivate: [canActivateAuthRole], data: { roles: ['USER']}},
  { path: 'myBalance', component: BalanceComponent, canActivate: [canActivateAuthRole], data: { roles: ['USER']}},
  { path: 'myLoans', component: LoansComponent, canActivate: [canActivateAuthRole], data: { roles: ['USER']}},
  { path: 'myCards', component: CardsComponent, canActivate: [canActivateAuthRole], data: { roles: ['ADMIN']}}
];
