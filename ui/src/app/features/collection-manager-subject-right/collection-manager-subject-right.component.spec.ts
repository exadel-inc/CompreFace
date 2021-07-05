import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CollectionManagerSubjectRightComponent } from './collection-manager-subject-right.component';

describe('CollectionManagerSubjectRightComponent', () => {
  let component: CollectionManagerSubjectRightComponent;
  let fixture: ComponentFixture<CollectionManagerSubjectRightComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CollectionManagerSubjectRightComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CollectionManagerSubjectRightComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
