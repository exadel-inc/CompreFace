import { Component, OnInit, Input } from '@angular/core';
import { TableComponent } from '../table/table.component';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ModelRelation } from 'src/app/data/modelRelation';

@Component({
  selector: 'app-model-relation-table',
  templateUrl: './model-relation-table.component.html',
  styleUrls: ['./model-relation-table.component.sass']
})
export class ModelRelationTableComponent extends TableComponent implements OnInit {
  @Input() availableRoles$: Observable<string[]>;

  ngOnInit() {
  }

  public isRoleChangeAllowed(shareMode: string): Observable<boolean> {
    return this.availableRoles$.pipe(map(availableRoles => !!~availableRoles.indexOf(shareMode)));
  }

  public onDelete(relation: ModelRelation): void {
    relation.shareMode = 'NONE';
    this.onChange.emit(relation);
  }
}
