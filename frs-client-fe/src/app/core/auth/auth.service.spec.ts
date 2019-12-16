import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth.service';
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {User} from "../../data/user";
import {environment} from "../../../environments/environment";
import {API_URL} from "../../data/api.variables";

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem('token', 'some token');
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.get(AuthService);
    httpMock = TestBed.get(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('be able to logIn', () => {
    const dummyUser: User = {
      username: 'username',
      password: 'password'
    };

    const dummyToken = "Some token";

    service.logIn(dummyUser.username, dummyUser.password).subscribe(token => {
      expect(token).toEqual(dummyToken);
    });

    const request = httpMock.expectOne( `${environment.apiUrl}${API_URL.LOGIN}`);
    expect(request.request.method).toBe('POST');
    request.flush(dummyToken);
  });

  it('be able to signUp', () => {
    const dummyUser: User = {
      username: 'username',
      password: 'password',
      email: 'q@q.com'
    };

    const dummyToken = "Some token";

    service.signUp(dummyUser.username, dummyUser.password, dummyUser.email).subscribe(token => {
      expect(token).toEqual(dummyToken);
    });

    const request = httpMock.expectOne( `${environment.apiUrl}${API_URL.REGISTER}`);
    expect(request.request.method).toBe('POST');
    request.flush(dummyToken, {status: 201, statusText: 'Created'});
  });

  it('be able to get token', () => {
    service.getToken().subscribe(token => expect(token).toEqual('some token'));
  });

  it('be able to update token', () => {
    const subscription = service.getToken().subscribe(token => expect(token).toEqual('some token'));
    subscription.unsubscribe();
    service.updateToken('token the second value');
    service.getToken().subscribe(token => expect(token).toEqual('token the second value'));
  });
});
