import { Component, OnInit } from '@angular/core';
import { TableComponent } from '../table/table.component';

@Component({
  selector: 'app-user-table',
  templateUrl: './user-table.component.html',
  styleUrls: ['./user-table.component.sass']
})
export class UserTableComponent extends TableComponent implements OnInit {
  public availableRoles: string[];

  ngOnInit() {
    this.availableRoles = [
      "OWNER",
      "ADMINISTRATOR",
      "USER"
    ];
  }
}
