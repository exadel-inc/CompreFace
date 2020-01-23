import { ChangeDetectionStrategy, Component, EventEmitter, OnInit, Input, Output } from '@angular/core';
import { Observable } from 'rxjs';

export interface ITableConfig {
  columns: {
    title: string;
    property: string;
  }[];
  data: any[];
}

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TableComponent implements OnInit {
  @Input() isLoading$: Observable<boolean>;
  @Input() set tableConfig(config: ITableConfig) {
    if (config) {
      this.columnsDefinition = config.columns;
      this.displayedColumns = config.columns.map(column => column.title);
      this.data = config.data;
    }
  }

  @Output() onChange = new EventEmitter<any>();

  public columnsDefinition: {
    title: string;
    property: string;
  }[];
  public displayedColumns: string[];
  public data: any[];

  constructor() { }

  ngOnInit() { }

  public change(element: any): void {
    this.onChange.emit(element);
  }
}
