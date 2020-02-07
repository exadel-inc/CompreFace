import {Component, Input, ChangeDetectionStrategy} from '@angular/core';
import {TableComponent} from '../table/table.component';
import {Observable} from 'rxjs';
import {ModelRelation} from 'src/app/data/modelRelation';

@Component({
  selector: 'app-model-relation-table',
  templateUrl: './model-relation-table.component.html',
  styleUrls: ['./model-relation-table.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ModelRelationTableComponent extends TableComponent {
  @Input() availableRoles$: Observable<string[]>;

  public onDelete(relation: ModelRelation): void {
    relation.shareMode = 'NONE';
    this.onChange.emit(relation);
  }
}
