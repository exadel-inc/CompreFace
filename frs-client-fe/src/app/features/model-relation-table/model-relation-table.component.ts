import { Component, OnInit, Input } from '@angular/core';
import { TableComponent, ITableConfig } from '../table/table.component';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-model-relation-table',
  templateUrl: './model-relation-table.component.html',
  styleUrls: ['./model-relation-table.component.sass']
})
export class ModelRelationTableComponent extends TableComponent implements OnInit {
  @Input() availableRoles$: Observable<string[]>;

  ngOnInit() {
  }
}
