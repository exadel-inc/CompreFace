import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { EntityTitleComponent } from './entity-title.component';
import {CommonModule} from "@angular/common";
import {MatInputModule} from "@angular/material/input";
import {FormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatSelectModule} from "@angular/material/select";
import {Subject} from "rxjs";

describe('EntityTitleComponent', () => {
  let component: EntityTitleComponent;
  let fixture: ComponentFixture<EntityTitleComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EntityTitleComponent ],
      imports: [
        CommonModule,
        MatButtonModule,
        MatInputModule,
        MatIconModule,
        MatSelectModule,
        FormsModule,
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EntityTitleComponent);
    component = fixture.componentInstance;
    component.options = [{
      id: '',
      name: 'someName',
      role: '',
    }];
    component.selectId$ = new Subject();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
