import { ChangeDetectionStrategy, Component, OnInit, OnDestroy } from '@angular/core';
import { UserListFacade } from './user-list-facade';
import { Observable, Subscription, of } from 'rxjs';
import { AppUser } from 'src/app/data/appUser';
import { map } from 'rxjs/operators';
import { ITableConfig } from '../table/table.component';

@Component({
  selector: 'user-list-container',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserListComponent implements OnInit, OnDestroy {
  public tableConfig$: Observable<ITableConfig>;
  public isLoading$: Observable<boolean>;
  public errorMessage: string;

  private userListStateSubscription: Subscription;

  constructor(private userListFacade: UserListFacade) {
    userListFacade.initSubscriptions();
  }

  ngOnInit() {
    this.userListStateSubscription = this.userListFacade.userListState$.subscribe(state => {
      this.errorMessage = state.errorMessage;
    });

    this.isLoading$ = this.userListFacade.userListState$.pipe(map(state => state.isLoading));

    this.tableConfig$ = this.userListFacade.users$.pipe(map((users: AppUser[]) => {
      return {
          columns: [{ title: 'user', property: 'username' }, { title: 'role', property: 'role' }],
          data: users
        }
    }))
  }

  public onChange(user: AppUser): void {
    this.userListFacade.updateUserRole(user.id, user.accessLevel);
  }

  public onInviteUser(email: string): void {
    this.userListFacade.inviteUser(email);
  }

  ngOnDestroy() {
    this.userListStateSubscription.unsubscribe();
    this.userListFacade.unsubscribe();
  }
}
