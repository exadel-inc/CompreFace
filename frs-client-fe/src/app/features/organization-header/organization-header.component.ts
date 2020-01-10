import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {Organization} from "../../data/organization";
import {OrganizationHeaderService} from "./organization-header.service";
import {Observable} from "rxjs";
import {CreateDialogComponent} from "../create-dialog/create-dialog.component";
import {MatDialog} from "@angular/material/dialog";

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

  constructor(private organizationHeaderService: OrganizationHeaderService, public dialog: MatDialog) { }

  ngOnInit() {
    this.organizations$ = this.organizationHeaderService.organization$;
    this.userRole$ = this.organizationHeaderService.userRole$;
    this.selectedId$ = this.organizationHeaderService.selectedId$
  }

  createNew() {
      const dialog = this.dialog.open(CreateDialogComponent, {
        data: {
          entityType: 'organization',
          name: ''
        }
      });

    dialog.afterClosed().subscribe(res => {
      if (res) {
        this.organizationHeaderService.add({
          name: res
        })
      }
    });
  }

  selectOrganization(id) {
    this.organizationHeaderService.select(id)
  }

  rename(name) {
    this.organizationHeaderService.rename(name)
  }
}
