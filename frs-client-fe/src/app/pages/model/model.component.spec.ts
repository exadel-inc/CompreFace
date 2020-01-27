import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {ModelComponent} from "./model.component";
import {ModelPageService} from "./model.service";
describe('ModelComponent', () => {
  let component: ModelComponent;
  let fixture: ComponentFixture<ModelComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ModelComponent ],
      providers: [
        {provide: ModelPageService, useValue: {
            initUrlBindingStreams: () => {},
            unSubscribe: () => {},
          }}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
