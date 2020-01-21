import { ChangeDetectionStrategy, Component, OnInit, OnDestroy } from '@angular/core';
import { UserListFacade } from './user-list-facade';
import { Observable } from 'rxjs';
import { AppUser } from 'src/app/data/appUser';
import { map } from 'rxjs/operators';
import { ITableConfig } from '../table/table.component';
import { MatDialog } from '@angular/material';
import { AlertComponent } from '../alert/alert.component';

@Component({
  selector: 'user-list-container',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserListComponent implements OnInit, OnDestroy {
  public tableConfig$: Observable<ITableConfig>;
  public isLoading$: Observable<boolean>;
  public availableRoles$: Observable<string[]>;
  public errorMessage: string;

  constructor(private userListFacade: UserListFacade, public dialog: MatDialog) {
    userListFacade.initSubscriptions();
  }

  ngOnInit() {
    this.isLoading$ = this.userListFacade.isLoading$;

    this.tableConfig$ = this.userListFacade.users$.pipe(map((users: AppUser[]) => {
      return {
          columns: [{ title: 'user', property: 'username' }, { title: 'role', property: 'role' }],
          data: users
        }
    }));

    this.availableRoles$ = this.userListFacade.availableRoles$;
  }

  public onChange(user: AppUser): void {
    this.userListFacade.updateUserRole(user.id, user.accessLevel);
  }

  public onInviteUser(email: string): void {
    this.userListFacade.inviteUser(email)
      .subscribe(() => this.openEmailNotification(email));
  }

  public ngOnDestroy(): void {
    this.userListFacade.unsubscribe();
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
