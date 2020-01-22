import { TestBed } from '@angular/core/testing';

import { ModelServiceService } from './model.service';

describe('ModelServiceService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ModelServiceService = TestBed.get(ModelServiceService);
    expect(service).toBeTruthy();
  });
});
