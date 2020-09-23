import { TestBed } from '@angular/core/testing';

import { DragNDropService } from './drag-n-drop.service';

describe('DragNDropService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: DragNDropService = TestBed.get(DragNDropService);
    expect(service).toBeTruthy();
  });
});
