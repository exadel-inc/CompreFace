import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ChangeDetectionStrategy, Component, OnInit, OnDestroy } from '@angular/core';
import { CreateDialogComponent } from 'src/app/features/create-dialog/create-dialog.component';
import { MatDialog } from '@angular/material';
import { ITableConfig } from 'src/app/features/table/table.component';
import { ModelListFacade } from './model-list-facade';
import {ROUTERS_URL} from "../../data/routers-url.variable";
import {Router} from "@angular/router";

@Component({
  selector: 'app-model-list',
  templateUrl: './model-list.component.html',
  styleUrls: ['./model-list.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ModelListComponent implements OnInit, OnDestroy {
  public isLoading$: Observable<boolean>;
  public errorMessage: string;
  public tableConfig$: Observable<ITableConfig>;

  constructor(private modelListFacade: ModelListFacade, public dialog: MatDialog, private router: Router) {
    this.modelListFacade.initSubscriptions();
  }

  ngOnInit() {
    this.isLoading$ = this.modelListFacade.isLoading$;

    this.tableConfig$ = this.modelListFacade.models$
      .pipe(
        map(models => {
          return ({
            columns: [{ title: 'Title', property: 'name' }, { title: 'Owner name', property: 'owner' }],
            data: models.map(model => ({ id: model.id, name: model.name, owner: model.owner.firstName }))
          })
        })
      );
  }

  public onClick(model): void {
    this.router.navigate([ROUTERS_URL.MODEL], {
      queryParams: {
        org: this.modelListFacade.selectedOrganizationId,
        app: this.modelListFacade.selectedApplicationId,
        model: model.id,
      }
    });
    console.log(`model ${model.name} was clicked`);
  }

  public onCreateNewModel(): void {
    const dialog = this.dialog.open(CreateDialogComponent, {
      width: '300px',
      data: {
        entityType: 'model',
        name: ''
      }
    });

    const dialogSubscription = dialog.afterClosed().subscribe(name => {
      if (name) {
        this.modelListFacade.createModel(name);
        dialogSubscription.unsubscribe();
      }
    });
  }

  ngOnDestroy(): void {
    this.modelListFacade.unsubscribe();
  }
}
