import { Component, EventEmitter, OnInit, Input, Output } from '@angular/core';

export interface ITableConfig {
  displayedColumns: string[];
  data: { [key: string]: string }[];
}

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.sass']
})
export class TableComponent implements OnInit {
  @Input() set tableConfig(config: ITableConfig) {
    this.displayedColumns = config.displayedColumns;
    this.data = config.data;
  }

  @Output() onChange = new EventEmitter<any>();

  public displayedColumns: string[];
  public data: any[];

  constructor() { }

  ngOnInit() {}

  public change(element: any): void {
    this.onChange.emit(element);
  }
}
