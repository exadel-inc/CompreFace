import { TestBed } from '@angular/core/testing';

import { ModelRelationService } from './model-relation.service';

describe('ModelRelationService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ModelRelationService = TestBed.get(ModelRelationService);
    expect(service).toBeTruthy();
  });
});
