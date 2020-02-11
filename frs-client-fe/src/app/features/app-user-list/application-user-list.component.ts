import { ChangeDetectionStrategy, Component, OnInit, OnDestroy } from '@angular/core';
import { ApplicationUserListFacade } from './application-user-list-facade';
import { Observable } from 'rxjs';
import { AppUser } from 'src/app/data/appUser';
import { map } from 'rxjs/operators';
import { ITableConfig } from '../table/table.component';
import { MatDialog } from '@angular/material';
import {AlertComponent} from '../alert/alert.component';

@Component({
  selector: 'app-application-user-list',
  templateUrl: './application-user-list.component.html',
  styleUrls: ['./application-user-list.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ApplicationUserListComponent implements OnInit, OnDestroy {
  public tableConfig$: Observable<ITableConfig>;
  public isLoading$: Observable<boolean>;
  public availableRoles$: Observable<string[]>;
  public errorMessage: string;
  public availableEmails$: Observable<string[]>;
  public search = '';

  constructor(private appUserListFacade: ApplicationUserListFacade, public dialog: MatDialog) {
    appUserListFacade.initSubscriptions();
  }

  ngOnInit() {
    this.isLoading$ = this.appUserListFacade.isLoading$;
    this.availableEmails$ = this.appUserListFacade.availableEmails$;

    this.tableConfig$ = this.appUserListFacade.appUsers$.pipe(map((users: AppUser[]) => {
      return {
          columns: [{ title: 'user', property: 'username' }, { title: 'role', property: 'role' }],
          data: users
      };
    }));

    this.availableRoles$ = this.appUserListFacade.availableRoles$;
  }

  public onChange(user: AppUser): void {
    this.appUserListFacade.updateUserRole(user.id, user.accessLevel);
  }

  public ngOnDestroy(): void {
    this.appUserListFacade.unsubscribe();
  }

  public onInviteUser(email: string): void {
    this.appUserListFacade.inviteUser(email)
      .subscribe(() => this.openEmailNotification(email));
  }

  private openEmailNotification(email: string): void {
    if (!email) {
      return;
    }

    this.dialog.open(AlertComponent, {
      data: {
        type: 'info',
        message: `Invitation was sent to ${email}`
      },
      minWidth: 300
    });
  }
}
