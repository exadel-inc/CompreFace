import { Component, EventEmitter, OnInit, Input, Output } from '@angular/core';

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
  styleUrls: ['./table.component.sass']
})
export class TableComponent implements OnInit {
  @Input() set tableConfig(config: ITableConfig) {
    this.columnsDefenition = config.columns;
    this.displayedColumns = config.columns.map(column => column.title);
    this.data = config.data;
  }

  @Output() onChange = new EventEmitter<any>();

  public columnsDefenition: {
    title: string;
    property: string;
  }[];
  public displayedColumns: string[];
  public data: any[];

  constructor() { }

  ngOnInit() {}

  public change(element: any): void {
    this.onChange.emit(element);
  }
}
