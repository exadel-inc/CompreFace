import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {environment} from '../../../environments/environment';
import {UserInfoService} from './user-info.service';
import {User} from '../../data/user';

describe('UserInfoService', () => {
  let service: UserInfoService;
  let httpMock: HttpTestingController;

  const mockData: User = {
    email: 'email',
    password: 'password',
    firstName: 'string',
    guid: 'guid_0',
    userId: '1',
    lastName: 'string',
    avatar: 'avatar.png'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserInfoService]
    });

    service = TestBed.get(UserInfoService);
    httpMock = TestBed.get(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return user info', () => {
    service.get().subscribe((data) => {
      expect(data).toEqual(mockData);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}user/me`);
    expect(req.request.method).toBe('GET');
    req.flush(mockData);
  });

});
