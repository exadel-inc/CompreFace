import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {AppUserService} from './app-user.service';

describe('AppUserService', () => {
  let service: AppUserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AppUserService]
    });

    service = TestBed.get(AppUserService);
    httpMock = TestBed.get(HttpTestingController);
  });

  it('should be created', () => {
    const service: AppUserService = TestBed.get(AppUserService);
    expect(service).toBeTruthy();
  });
});
