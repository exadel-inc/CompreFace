import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material';
import { SpinnerModule } from '../spinner/spinner.module';
import { TableModule } from '../table/table.module';
import { ModelListFacade } from './model-list-facade';
import { of } from 'rxjs';

import { ModelListComponent } from './model-list.component';
import {Router} from "@angular/router";

describe('ModelListComponent', () => {
  let component: ModelListComponent;
  let fixture: ComponentFixture<ModelListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ModelListComponent],
      imports: [SpinnerModule, TableModule],
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
          unsubscribe(){}
        }
      },
        {provide: Router}
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
