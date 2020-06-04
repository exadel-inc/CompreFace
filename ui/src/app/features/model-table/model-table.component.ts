import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { Model } from 'src/app/data/model';

import { TableComponent } from '../table/table.component';

@Component({
  selector: 'app-model-table',
  templateUrl: './model-table.component.html',
  styleUrls: ['./model-table.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ModelTableComponent extends TableComponent {
  @Input() userRole: string;
  @Output() copyApiKey = new EventEmitter<string>();
  @Output() edit = new EventEmitter<Model>();
  @Output() delete = new EventEmitter<Model>();
}
