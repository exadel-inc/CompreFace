import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { provideMockStore, MockStore } from '@ngrx/store/testing';

import { ApplicationListComponent } from './application-list-container.component';
import { MatDialog } from '@angular/material';
import { AppState } from 'src/app/store';
import { SpinnerModule } from '../spinner/spinner.module';
import { TableModule } from '../table/table.module';
import { ApplicationListFacade } from './application-list-facade';
import { Router } from '@angular/router';
import { of } from 'rxjs';

describe('ApplicationListComponent', () => {
  let component: ApplicationListComponent;
  let fixture: ComponentFixture<ApplicationListComponent>;
  let mockStore: MockStore<AppState>;
  let pipeMock = {
    pipe(fn) { return fn([{}]) }
  }

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ApplicationListComponent],
      imports: [SpinnerModule, TableModule],
      providers: [provideMockStore({
        initialState: {
          applicationList: {},
          application: {
            ids: [],
            entities: []
          }
        }
      }), {
        provide: MatDialog,
        useValue: {}
      }, {
        provide: ApplicationListFacade,
        useValue: {
          initSubscriptions: () => of([{}]),
          applications$: of([{
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
      }, {
        provide: Router,
        useValue: {}
      }]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
