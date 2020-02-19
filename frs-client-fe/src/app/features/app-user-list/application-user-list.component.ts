import {ChangeDetectionStrategy, Component, OnInit, OnDestroy} from '@angular/core';
import {ApplicationUserListFacade} from './application-user-list-facade';
import {Observable, Subscription} from 'rxjs';
import {AppUser} from 'src/app/data/appUser';
import {map} from 'rxjs/operators';
import {ITableConfig} from '../table/table.component';
import {MatDialog} from '@angular/material';
import {SnackBarService} from '../snackbar/snackbar.service';
import {InviteDialogComponent} from '../invite-dialog/invite-dialog.component';

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
  public availableRoles: string[];
  private availableRolesSubscription: Subscription;

  constructor(
    private appUserListFacade: ApplicationUserListFacade,
    public dialog: MatDialog,
    private snackBarService: SnackBarService
  ) {
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
    this.availableRolesSubscription = this.appUserListFacade.availableRoles$.subscribe(value => this.availableRoles = value);
  }

  public onChange(user: AppUser): void {
    this.appUserListFacade.updateUserRole(user.id, user.accessLevel);
  }

  public ngOnDestroy(): void {
    this.appUserListFacade.unsubscribe();
    this.availableRolesSubscription.unsubscribe();
  }

  public onInviteUser(email: string): void {
    // this.appUserListFacade.inviteUser(email)
    //   .subscribe(() => this.openEmailNotification(email));
    const dialog = this.dialog.open(InviteDialogComponent, {
      data: {
        availableRoles: this.availableRoles,
        options$: this.availableEmails$
      }
    });

    const dialogSubscription = dialog.afterClosed().subscribe(({userEmail, role}) => {
      if (userEmail && role) {
        this.appUserListFacade.inviteUser(userEmail, role).subscribe(() => this.openEmailNotification(userEmail));
        dialogSubscription.unsubscribe();
      }
    });
  }

  private openEmailNotification(email: string): void {
    if (!email) {
      return;
    }

    this.snackBarService.openInfo(void 0, void 0, `Invitation was sent to ${email}`);
  }
}
