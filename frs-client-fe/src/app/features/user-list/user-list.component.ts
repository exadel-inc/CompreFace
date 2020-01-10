import { ChangeDetectionStrategy, Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { UserListFacade } from './user-list-facade.service';
import { Observable, Subscription } from 'rxjs';
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
  public isLoading: boolean;
  public errorMessage: string;

  private userListStateSubscription: Subscription;

  constructor(private userListFacade: UserListFacade, private cdr: ChangeDetectorRef) {
    userListFacade.initSubscriptions();
  }

  ngOnInit() {
    this.userListStateSubscription = this.userListFacade.userListState$.subscribe(state => {
      this.isLoading = state.isLoading;
      this.errorMessage = state.errorMessage;
      this.cdr.markForCheck();
    });

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

  ngOnDestroy() {
    this.userListStateSubscription.unsubscribe();
    this.userListFacade.unsubscribe();
  }
}
