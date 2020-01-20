import { Component, OnInit, Input } from '@angular/core';
import { TableComponent } from '../table/table.component';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-user-table',
  templateUrl: './user-table.component.html',
  styleUrls: ['./user-table.component.sass']
})
export class UserTableComponent extends TableComponent implements OnInit {
  public availableRoles: string[];
  @Input() availableRoles$: Observable<string[]>;

  public isRoleChangeAllowed(userRole: string): Observable<boolean> {
    return this.availableRoles$.pipe(map(availableRoles => !!~availableRoles.indexOf(userRole)));
  }
}
