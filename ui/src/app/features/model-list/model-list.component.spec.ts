import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';
import { of } from 'rxjs';

import { ModelTableModule } from '../model-table/model-table.module';
import { SpinnerModule } from '../spinner/spinner.module';
import { ModelListFacade } from './model-list-facade';
import { ModelListComponent } from './model-list.component';

describe('ModelListComponent', () => {
  let component: ModelListComponent;
  let fixture: ComponentFixture<ModelListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ModelListComponent],
      imports: [SpinnerModule, ModelTableModule],
      providers: [{
        provide: MatDialog,
        useValue: {}
      }, {
        provide: ModelListFacade,
        useValue: {
          initSubscriptions: () => of([{}]),
          models$: of([{
            id: 0,
            name: 'name',
            owner: {
              firstname: 'firstname'
            }
          }]),
          selectedOrganization$: of([{}]),
          isLoading$: of([{}]),
          unsubscribe() {
          }
        }
      },
      { provide: Router }
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModelListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
