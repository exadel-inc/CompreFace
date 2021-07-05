import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CollectionManagerSubjectLeftComponent } from './collection-manager-subject-left.component';

describe('CollectionManagerSubjectLeftComponent', () => {
  let component: CollectionManagerSubjectLeftComponent;
  let fixture: ComponentFixture<CollectionManagerSubjectLeftComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CollectionManagerSubjectLeftComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CollectionManagerSubjectLeftComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
