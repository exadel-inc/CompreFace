import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {Application} from "../../data/application";
import {ApplicationHeaderFacade} from "./application-header.facade";
import {Observable} from "rxjs";
import {CreateDialogComponent} from "../create-dialog/create-dialog.component";
import {MatDialog} from "@angular/material/dialog";

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

  constructor(private applicationHeaderFacade: ApplicationHeaderFacade, public dialog: MatDialog) {
    applicationHeaderFacade.initSubscriptions();
  }

  ngOnInit() {
    this.app$ = this.applicationHeaderFacade.app$;
    // this.userRole$ = this.applicationHeaderFacade.userRole$;
    // this.selectedId$ = this.applicationHeaderFacade.selectedId$
  }

  createNew() {
      const dialog = this.dialog.open(CreateDialogComponent, {
        data: {
          entityType: 'application',
          name: ''
        }
      });

    dialog.afterClosed().subscribe(res => {
      if (res) {
        this.applicationHeaderFacade.add({
          name: res
        })
      }
    });
  }

  selectOrganization(id) {
    // this.applicationHeaderFacade.select(id)
  }

  rename(name) {
    this.applicationHeaderFacade.rename(name)
  }

}
