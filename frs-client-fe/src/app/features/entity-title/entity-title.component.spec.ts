import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EntityTitleComponent } from './entity-title.component';

describe('EntityTitleComponent', () => {
  let component: EntityTitleComponent;
  let fixture: ComponentFixture<EntityTitleComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EntityTitleComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EntityTitleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
