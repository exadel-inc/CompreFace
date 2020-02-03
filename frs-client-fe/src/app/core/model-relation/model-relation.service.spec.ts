import { TestBed } from '@angular/core/testing';

import { ModelRelationService } from './model-relation.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('ModelRelationService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule]
  }));

  it('should be created', () => {
    const service: ModelRelationService = TestBed.get(ModelRelationService);
    expect(service).toBeTruthy();
  });
});
