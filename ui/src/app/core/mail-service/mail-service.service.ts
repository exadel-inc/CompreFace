import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API } from 'src/app/data/enums/api-url.enum';
import { MailServiceStatus } from 'src/app/data/interfaces/mail-service-status';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class MailService {
  constructor(private http: HttpClient) {}

  getStatus(): Observable<MailServiceStatus> {
    const url = `${environment.adminApiUrl}${API.MailServiceStatus}`;

    return this.http.get<MailServiceStatus>(url);
  }
}
