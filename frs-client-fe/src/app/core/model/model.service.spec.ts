import { TestBed } from '@angular/core/testing';

import { ModelService } from './model.service';
import { HttpTestingController, HttpClientTestingModule } from '@angular/common/http/testing';
import { environment } from "../../../environments/environment";
import { Model } from 'src/app/data/model';

describe('ModelService', () => {
  let httpMock: HttpTestingController;
  const mockModels: Model[] = [
    {
      "id": '0',
      "name": "Model 1",
      "accessLevel": "OWNER/TRAIN/READONLY",
      "applicationId": [{
        "id": "app_0",
        "shareMode": "NONE"
      }],
      "owner": {
        "id": "0",
        "firstName": "Owner 0",
        "lastName": "lastname owner 0"
      }
    },
    {
      "id": '1',
      "name": "Model 2",
      "accessLevel": "OWNER/TRAIN/READONLY",
      "applicationId": [{
        "id": "app_0",
        "shareMode": "NONE"
      }],
      "owner": {
        "id": "0",
        "firstName": "Owner 0",
        "lastName": "lastname owner 0"
      }
    }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    httpMock = TestBed.get(HttpTestingController);
  });

  it('should be created', () => {
    const service: ModelService = TestBed.get(ModelService);
    expect(service).toBeTruthy();
  });

  it('should return models array', () => {
    const service: ModelService = TestBed.get(ModelService);
    service.getAll('0', '0').subscribe(data => {
      expect(data).toEqual(mockModels);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}org/0/app/0/models`);
    expect(req.request.method).toBe('GET');
    req.flush(mockModels);
  });

  it('should return created model', () => {
    const service: ModelService = TestBed.get(ModelService);
    service.create('0', 'app_0', 'new model').subscribe(data => {
      expect(data.name).toEqual('new model');
      expect(data.applicationId[0].id).toEqual('app_0');
    });
    const req = httpMock.expectOne(`${environment.apiUrl}org/0/app/app_0/model`);
    expect(req.request.method).toBe('POST');
    req.flush({
      "id": '2',
      "name": "new model",
      "accessLevel": "OWNER/TRAIN/READONLY",
      "applicationId": [{
        "id": "app_0",
        "shareMode": "NONE"
      }],
      "owner": {
        "id": "0",
        "firstName": "Owner 0",
        "lastName": "lastname owner 0"
      }
    });
  })
});
