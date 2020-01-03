import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from "@angular/common/http/testing";
import { ApplicationService } from './application.service';
import { Application } from 'src/app/data/application';
import { environment } from "../../../environments/environment";

describe('ApplicationService', () => {
  let service: ApplicationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApplicationService]
    });

    service = TestBed.get(ApplicationService);
    httpMock = TestBed.get(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return set of applications', () => {
    service.getAll('organizationId').subscribe((data: Application[]) => {
      expect(data.length).toBe(2);

      expect(data[0].name).toBe('test-application-one');
      expect(data[0].organizationId).toBe('organizationId');

      expect(data[1].name).toBe('test-application-two');
      expect(data[1].organizationId).toBe('organizationId');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/org/organizationId/apps`);
    expect(req.request.method).toBe('GET');
    req.flush([{
      name: 'test-application-one',
      id: '0',
      owner: 'owner',
      organizationId: 'organizationId'
    }, {
      name: 'test-application-two',
      id: '1',
      owner: 'owner',
      organizationId: 'organizationId'
    }]);
  });

  it('should return set of applications', () => {
    service.create('organizationId', 'new-app').subscribe((data: Application) => {
      expect(data.name).toBe('new-app');
      expect(data.organizationId).toBe('organizationId');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/org/organizationId/app`);
    expect(req.request.method).toBe('POST');
    req.flush({
      name: 'new-app',
      id: '2',
      owner: 'owner',
      organizationId: 'organizationId'
    });
  });
});
