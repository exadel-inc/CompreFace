import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { OrganizationComponent } from './organization.component';
import { ApplicationListComponent } from './components/application-list/application-list-container.component';
import { AppState } from 'src/app/store';
import { MatDialog } from '@angular/material';

describe('OrganizationComponent', () => {
  let component: OrganizationComponent;
  let fixture: ComponentFixture<OrganizationComponent>;
  let mockStore: MockStore<AppState>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OrganizationComponent, ApplicationListComponent ],
      providers: [provideMockStore({
        initialState: {
          applicationList: {}
        }
      }), { provide: MatDialog, useValue: {}}]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OrganizationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
