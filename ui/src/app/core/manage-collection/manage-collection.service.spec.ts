import { TestBed } from '@angular/core/testing';

import { ManageCollectionService } from './manage-collection.service';

describe('ManageCollectionService', () => {
  let service: ManageCollectionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ManageCollectionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
