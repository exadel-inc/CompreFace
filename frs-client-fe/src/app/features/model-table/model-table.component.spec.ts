import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatButtonModule, MatMenuModule, MatTableModule } from '@angular/material';
import { MatIconModule } from '@angular/material/icon';

import { ModelTableComponent } from './model-table.component';

describe('ModelTableComponent', () => {
  let component: ModelTableComponent;
  let fixture: ComponentFixture<ModelTableComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ModelTableComponent],
      imports: [
        MatTableModule,
        MatButtonModule,
        MatIconModule,
        MatMenuModule,
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModelTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
