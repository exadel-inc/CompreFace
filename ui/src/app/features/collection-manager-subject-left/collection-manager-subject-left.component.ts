import { Component } from '@angular/core';
import {CreateDialogComponent} from "../create-dialog/create-dialog.component";
import {TranslateService} from "@ngx-translate/core";
import {MatDialog} from "@angular/material/dialog";
import {ApplicationListFacade} from "../application-list/application-list-facade";

@Component({
  selector: 'app-collection-manager-subject-left',
  templateUrl: './collection-manager-subject-left.component.html',
  styleUrls: ['./collection-manager-subject-left.component.scss'],
})
export class CollectionManagerSubjectLeftComponent {
  testArr = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
  constructor( private translate: TranslateService,
               private dialog: MatDialog,
               private applicationFacade: ApplicationListFacade,) {}

    openModalWindow() {
      const dialog = this.dialog.open(CreateDialogComponent, {
        panelClass: 'custom-mat-dialog',
        data: {
          entityType: this.translate.instant('applications.header.title'),
          name: '',
        },
      });

      const dialogSubscription = dialog.afterClosed().subscribe(name => {
        if (name) {
          this.applicationFacade.createApplication(name);
          dialogSubscription.unsubscribe();
        }
      });
    }
}
