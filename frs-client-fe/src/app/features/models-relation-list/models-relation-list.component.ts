import {ChangeDetectionStrategy, Component, OnInit, OnDestroy} from '@angular/core';
import {ModelRelationListFacade} from './model-relation-list-facade';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {ITableConfig} from '../table/table.component';
import {Application} from 'src/app/data/application';
import {ModelRelation} from 'src/app/data/modelRelation';

@Component({
  selector: 'app-models-relation-list',
  templateUrl: './models-relation-list.component.html',
  styleUrls: ['./models-relation-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ModelsRelationListComponent implements OnInit, OnDestroy {
  public isLoading$: Observable<boolean>;
  public tableConfig$: Observable<ITableConfig>;
  public availableRoles$: Observable<string[]>;

  constructor(private modelRelationListFacade: ModelRelationListFacade) {
    this.modelRelationListFacade.initSubscriptions();
   }

  ngOnInit() {
    this.isLoading$ = this.modelRelationListFacade.isLoading$;

    this.tableConfig$ = this.modelRelationListFacade.applications$.pipe(map((users: Application[]) => {
      return {
          columns: [
            { title: 'name', property: 'username' },
            { title: 'owner', property: 'owner' },
            { title: 'shareMode', property: 'shareMode'},
            { title: 'deleteAccess', property: 'role' }
          ],
          data: users
      };
    }));

    this.availableRoles$ = this.modelRelationListFacade.availableRoles$;
  }

  public onChange(relation: ModelRelation): void {
    this.modelRelationListFacade.updateUserRole(relation.id, relation.shareMode);
  }

  ngOnDestroy() {
    this.modelRelationListFacade.unsubscribe();
  }
}
