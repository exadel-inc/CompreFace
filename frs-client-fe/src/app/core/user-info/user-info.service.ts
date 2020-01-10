import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {User} from "../../data/user";
import {API_URL} from "../../data/api.variables";

@Injectable({
  providedIn: 'root'
})
export class UserInfoService {

  constructor(private http: HttpClient) {}

  public get(): Observable<User> {
    return this.http.get<User>(`${environment.apiUrl}${API_URL.GET_USER_INFO}`);
  }
}
