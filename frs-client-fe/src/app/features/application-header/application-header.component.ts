import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {Application} from "../../data/application";
import {ApplicationHeaderFacade} from "./application-header.facade";
import {Observable} from "rxjs";

@Component({
  selector: 'app-application-header',
  templateUrl: './application-header.component.html',
  styleUrls: ['./application-header.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class ApplicationHeaderComponent implements OnInit {
  public app$: Observable<Application>;
  public userRole$: Observable<string | null>;
  public selectedId$: Observable<any>;
  public loading$: Observable<boolean>;

  constructor(private applicationHeaderFacade: ApplicationHeaderFacade) {}

  ngOnInit() {
    this.app$ = this.applicationHeaderFacade.app$;
    this.userRole$ = this.applicationHeaderFacade.userRole$;
    this.selectedId$ = this.applicationHeaderFacade.selectedId$;
    this.loading$ = this.applicationHeaderFacade.loading$;
  }

  rename(name) {
    this.applicationHeaderFacade.rename(name)
  }
}
