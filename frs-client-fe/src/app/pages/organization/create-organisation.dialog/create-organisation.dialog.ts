import {Component} from '@angular/core';
import {MatDialogRef} from '@angular/material';

import {OrganizationService} from '../organization.service';
import {Router} from '@angular/router';
import {ROUTERS_URL} from '../../../data/routers-url.variable';

@Component({
  selector: 'app-create-organisation-dialog',
  templateUrl: './create-organisation.dialog.html',
  styleUrls: ['./create-organisation.dialog.scss'],
})
export class CreateOrganisationDialogComponent {
  public name = '';

  constructor(
    public dialogRef: MatDialogRef<CreateOrganisationDialogComponent>,
    private organizationService: OrganizationService,
    private router: Router,
  ) { }

  public close() {
    this.dialogRef.close();
  }

  public submit() {
    this.organizationService.createOrganization(this.name)
      .subscribe((org) => {
        this.close();
        this.router.navigate([ROUTERS_URL.ORGANIZATION, org.id]);
      });
  }
}
