import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {ModelsRelationListComponent} from './models-relation-list.component';
import {CommonModule} from '@angular/common';
import {ModelRelationTableModule} from '../model-relation-table/model-relation-table.module';
import {SpinnerModule} from '../spinner/spinner.module';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ModelRelationListFacade} from './model-relation-list-facade';
import {MatFormFieldModule, MatInputModule, MatButtonModule} from '@angular/material';
import {of} from 'rxjs/internal/observable/of';

describe('ModelsRelationListComponent', () => {
  let component: ModelsRelationListComponent;
  let fixture: ComponentFixture<ModelsRelationListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ModelsRelationListComponent ],
      imports: [
        CommonModule,
        ModelRelationTableModule,
        SpinnerModule,
        FormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        ReactiveFormsModule
      ],
      providers: [{
        provide: ModelRelationListFacade,
        useValue: {
          initSubscriptions: () => of([{}]),
          applications$: of([{
            id: 0,
            name: 'name',
            shareMode: 'value',
            owner: {
              firstname: 'firstname'
            }
          }]),
          selectedOrganization$: of([{}]),
          isLoading$: of([{}]),
          unsubscribe() { }
        }
      }]
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
