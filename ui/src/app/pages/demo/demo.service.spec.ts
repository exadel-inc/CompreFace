import { TestBed } from '@angular/core/testing';

import { DemoService } from './demo.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('DemoService', () => {
  let service: DemoService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ]
    });
    service = TestBed.inject(DemoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
