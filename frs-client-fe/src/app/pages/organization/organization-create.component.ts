import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material';
import {CreateOrganisationDialogComponent} from './create-organisation.dialog/create-organisation.dialog';
import {OrganizationService} from './organization.service';

@Component({
  selector: 'app-organization-create',
  templateUrl: './organization-create.component.html',
  styleUrls: ['./organization.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrganizationCreateComponent implements OnInit, OnDestroy {
  constructor(
    public dialog: MatDialog,
    private organizationService: OrganizationService,
  ) { }

  openDialog(): void {
    this.dialog.open(CreateOrganisationDialogComponent, {
      width: '320px',
      height: '200px',
      disableClose: true,
      panelClass: 'dialog-container',
    });
  }

  ngOnInit() {
    this.organizationService.initUrlBindingStreams();
  }

  ngOnDestroy() {
    this.organizationService.unSubscribe();
  }
}
