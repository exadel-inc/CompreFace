import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ServerStatusInt } from 'src/app/store/servers-status/reducers';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ServerStatusService {
  constructor(private http: HttpClient) {}

  getServerStatus(): Observable<ServerStatusInt> {
    const url = `${environment.adminApiUrl}status`;

    return this.http.get<ServerStatusInt>(url);
  }
}
