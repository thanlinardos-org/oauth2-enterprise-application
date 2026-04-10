import {Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {AppConstants} from "../../constants/app.constants";
import {environment} from '../../../environments/environment';
import {Contact} from '../../model/contact.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  constructor(private readonly http: HttpClient) {
  }

  getAccountDetails(email: string) {
    return this.http.get(environment.rootUrl + AppConstants.ACCOUNT_API_URL + "?email=" + email, {
      observe: 'response',
      withCredentials: true
    });
  }

  getAccountTransactions(email: string) {
    return this.http.get(environment.rootUrl + AppConstants.BALANCE_API_URL + "?email=" + email, {
      observe: 'response',
      withCredentials: true
    });
  }

  getLoansDetails(email: string) {
    return this.http.get(environment.rootUrl + AppConstants.LOANS_API_URL + "?email=" + email, {
      observe: 'response',
      withCredentials: true
    });
  }

  getCardsDetails(email: string) {
    return this.http.get(environment.rootUrl + AppConstants.CARDS_API_URL + "?email=" + email, {
      observe: 'response',
      withCredentials: true
    });
  }

  getNoticeDetails() {
    return this.http.get(environment.rootUrl + AppConstants.NOTICES_API_URL, {observe: 'response'});
  }

  saveMessage(contact: Contact) {
    return this.http.post(environment.rootUrl + AppConstants.CONTACT_API_URL, contact, {
      observe: 'response',
      withCredentials: true
    });
  }
}
