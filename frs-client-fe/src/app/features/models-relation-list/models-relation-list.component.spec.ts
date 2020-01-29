import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModelsRelationListComponent } from './models-relation-list.component';

describe('ModelsRelationListComponent', () => {
  let component: ModelsRelationListComponent;
  let fixture: ComponentFixture<ModelsRelationListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ModelsRelationListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModelsRelationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
