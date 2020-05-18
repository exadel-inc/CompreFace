import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppUser } from 'src/app/data/appUser';

import { TableComponent } from '../table/table.component';


@Component({
  selector: 'app-user-table',
  templateUrl: './user-table.component.html',
  styleUrls: ['./user-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserTableComponent extends TableComponent implements OnInit {
  @Input() availableRoles$: Observable<string[]>;
  @Input() currentUserId: string;
  @Output() deleteUser = new EventEmitter<AppUser>();

  isRoleChangeAllowed(userRole: string): Observable<boolean> {
    return this.availableRoles$.pipe(map(availableRoles => availableRoles.indexOf(userRole) > -1));
  }

  delete(user: AppUser): void {
    this.deleteUser.emit(user);
  }
}
