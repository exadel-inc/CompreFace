import {ChangeDetectionStrategy, Component, OnInit, OnDestroy} from '@angular/core';
import {UserListFacade} from './user-list-facade';
import {Observable, Subscription} from 'rxjs';
import {AppUser} from 'src/app/data/appUser';
import {map} from 'rxjs/operators';
import {ITableConfig} from '../table/table.component';
import {SnackBarService} from '../snackbar/snackbar.service';
import {InviteDialogComponent} from '../invite-dialog/invite-dialog.component';
import {MatDialog} from '@angular/material';

@Component({
  selector: 'app-user-list-container',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserListComponent implements OnInit, OnDestroy {
  public tableConfig$: Observable<ITableConfig>;
  public isLoading$: Observable<boolean>;
  public availableRoles: string[];
  public availableRoles$: Observable<string[]>;
  public errorMessage: string;
  public search = '';
  public availableRolesSubscription: Subscription;

  constructor(private userListFacade: UserListFacade, private snackBarService: SnackBarService, public dialog: MatDialog) {
    userListFacade.initSubscriptions();
  }

  ngOnInit() {
    this.isLoading$ = this.userListFacade.isLoading$;

    this.tableConfig$ = this.userListFacade.users$.pipe(map((users: AppUser[]) => {
      return {
          columns: [{ title: 'user', property: 'username' }, { title: 'role', property: 'role' }, {title: 'delete', property: 'delete'}],
          data: users
      };
    }));

    this.availableRoles$ = this.userListFacade.availableRoles$;
    this.availableRolesSubscription = this.userListFacade.availableRoles$.subscribe(value => this.availableRoles = value);
  }

  public onChange(user: AppUser): void {
    this.userListFacade.updateUserRole(user.id, user.role);
  }

  public onDelete(user: AppUser): void {
    this.userListFacade.deleteUser(user.userId);
  }

  public onInviteUser(): void {
    const dialog = this.dialog.open(InviteDialogComponent, {
      data: {
        availableRoles: this.availableRoles
      }
    });

    const dialogSubscription = dialog.afterClosed().subscribe(({userEmail, role}) => {
      if (userEmail && role) {
        this.userListFacade.inviteUser(userEmail, role).subscribe(() => this.openEmailNotification(userEmail));
        dialogSubscription.unsubscribe();
      }
    });
  }

  public ngOnDestroy(): void {
    this.userListFacade.unsubscribe();
    this.availableRolesSubscription.unsubscribe();
  }

  private openEmailNotification(email: string): void {
    if (!email) {
      return;
    }

    this.snackBarService.openInfo(void 0, void 0, `Invitation was sent to ${email}`);
  }
}
