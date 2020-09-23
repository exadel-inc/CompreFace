import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DragNDrop.ContainerComponent } from './drag-n-drop.container.component';

describe('DragNDrop.ContainerComponent', () => {
  let component: DragNDrop.ContainerComponent;
  let fixture: ComponentFixture<DragNDrop.ContainerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DragNDrop.ContainerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DragNDrop.ContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
