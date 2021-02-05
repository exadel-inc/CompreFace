/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

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
  styleUrls: ['./table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TableComponent {
  @Input() isLoading: boolean;
  @Input() set tableConfig(config: ITableConfig) {
    if (config) {
      this.columnsDefinition = config.columns;
      this.displayedColumns = config.columns.map(column => column.title);
      this.data = config.data;
    }
  }

  @Output() changeRow = new EventEmitter<any>();

  maxElementLength = 27;

  columnsDefinition: {
    title: string;
    property: string;
  }[];
  displayedColumns: string[];
  data: any[];

  change(element: any): void {
    this.changeRow.emit(element);
  }

  disableToolTip(element: string): boolean {
    return element.length <= this.maxElementLength;
  }
}
