import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { of } from 'rxjs';
import { switchMap, filter, tap, catchError, withLatestFrom } from 'rxjs/operators';
import { ModelService } from 'src/app/core/model/model.service';
import { Statistics } from 'src/app/data/interfaces/statistics';
import { selectCurrentAppId } from '../application/selectors';
import { selectCurrentModelId } from '../model/selectors';
import { loadModelStatistics, loadModelStatisticsFail, loadModelStatisticsSuccess } from './actions';

@Injectable()
export class ModelStatisticsEffect {
  constructor(private actions: Actions, private modelService: ModelService, private store: Store<any>) {}

  @Effect({ dispatch: false })
  loadModelStatistics$ = this.actions.pipe(
    ofType(loadModelStatistics),
    switchMap(data =>
      this.modelService.getStatistics(data.appId, data.modelId).pipe(
        // filter(data => !!data),
        tap((data: Statistics) => this.store.dispatch(loadModelStatisticsSuccess(data))),
        catchError(error => of(loadModelStatisticsFail(error)))
      )
    )
  );
}
