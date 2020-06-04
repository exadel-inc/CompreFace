import {TestBed} from '@angular/core/testing';
import {UserService} from './user.service';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {environment} from '../../../environments/environment';
import {AppUser} from '../../data/appUser';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserService]
    });

    service = TestBed.get(UserService);
    httpMock = TestBed.get(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return set of users', () => {
    const mock = [{
      id: 0,
      organizationId: 'ksdfklsn1111111',
      firstName: 'John',
      lastName: 'Malkovich',
      role: 'USER'
    }, {
      id: 1,
      organizationId: 'ksdfklsn1111111',
      firstName: 'Tony',
      lastName: 'Stark',
      role: 'ADMINISTRATOR'
    }];

    service.getAll('organizationId').subscribe((data: AppUser[]) => {
      expect(data[0].role).toBe(mock[0].role);
      expect(data[0].firstName).toBe(mock[0].firstName);
      expect(data[0].lastName).toBe(mock[0].lastName);
      expect(data[1].role).toBe(mock[1].role);
      expect(data[1].firstName).toBe(mock[1].firstName);
      expect(data[1].lastName).toBe(mock[1].lastName);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}org/organizationId/roles`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('should update user role', () => {
    const mock = {
      id: 'userId',
      organizationId: 'ksdfklsn1111111',
      firstName: 'John',
      lastName: 'Malkovich',
      role: 'USER'
    };

    service.updateRole('organizationId', 'userId', 'USER').subscribe((data: AppUser) => {
      expect(data.role).toBe(mock.role);
      expect(data.firstName).toBe(mock.firstName);
      expect(data.lastName).toBe(mock.lastName);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}org/organizationId/role`);
    expect(req.request.method).toBe('PUT');
    req.flush(mock);
  });
});
