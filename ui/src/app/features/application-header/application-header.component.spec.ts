import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {ApplicationHeaderComponent} from './application-header.component';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {EntityTitleModule} from '../entity-title/entity-title.module';
import {ApplicationHeaderFacade} from './application-header.facade';
import {SpinnerModule} from '../spinner/spinner.module';
import {MatCardModule, MatDialog} from '@angular/material';
import {Subject} from 'rxjs';

describe('ApplicationHeaderComponent', () => {
  let component: ApplicationHeaderComponent;
  let fixture: ComponentFixture<ApplicationHeaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplicationHeaderComponent ],
      providers: [
        {
          provide: ApplicationHeaderFacade,
          useValue: {
            rename: () => {},
            initSubscriptions: () => {},
            unsubscribe: () => {},
            app$: new Subject(),
          }
        },
        {provide: MatDialog, useValue: {}}
      ],
      imports: [
        CommonModule,
        RouterModule,
        MatButtonModule,
        SpinnerModule,
        EntityTitleModule,
        MatCardModule,
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
