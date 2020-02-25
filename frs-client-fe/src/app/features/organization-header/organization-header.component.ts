import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {Organization} from '../../data/organization';
import {OrganizationHeaderFacade} from './organization-header.facade';
import {Observable} from 'rxjs';
import {CreateDialogComponent} from '../create-dialog/create-dialog.component';
import {MatDialog} from '@angular/material/dialog';
import {EditDialogComponent} from '../edit-dialog/edit-dialog.component';

@Component({
  selector: 'app-organization-header',
  templateUrl: './organization-header.component.html',
  styleUrls: ['./organization-header.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrganizationHeaderComponent implements OnInit {
  public organizations$: Observable<Organization[]>;
  public userRole$: Observable<string | null>;
  public selectedId$: Observable<any>;

  constructor(private organizationHeaderFacade: OrganizationHeaderFacade, public dialog: MatDialog) {
    organizationHeaderFacade.initSubscriptions();
  }

  ngOnInit() {
    this.organizations$ = this.organizationHeaderFacade.organizations$;
    this.userRole$ = this.organizationHeaderFacade.userRole$;
    this.selectedId$ = this.organizationHeaderFacade.selectedId$;
  }

  createNew() {
    const dialog = this.dialog.open(CreateDialogComponent, {
      width: '300px',
      data: {
        entityType: 'organization',
        name: ''
      }
    });

    dialog.afterClosed().subscribe(res => {
      if (res) { this.organizationHeaderFacade.add({ name: res }); }
    });
  }

  edit() {
    let currentName = '';
    this.organizationHeaderFacade.organizationName$.subscribe(name => {
      currentName = name;
    });
    const dialog = this.dialog.open(EditDialogComponent, {
      width: '300px',
      data: {
        entityType: 'organization',
        entityName: currentName,
        name: ''
      }
    });

    dialog.afterClosed().subscribe(res => {
      if (res) { this.organizationHeaderFacade.rename(res); }
    });
  }

  selectOrganization(id) {
    this.organizationHeaderFacade.select(id);
  }

  rename(name) {
    this.organizationHeaderFacade.rename(name);
  }

  delete() {
    this.organizationHeaderFacade.delete();
  }
}
