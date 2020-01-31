import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModelRelationTableComponent } from './model-relation-table.component';

describe('ModelRelationTableComponent', () => {
  let component: ModelRelationTableComponent;
  let fixture: ComponentFixture<ModelRelationTableComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ModelRelationTableComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModelRelationTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
