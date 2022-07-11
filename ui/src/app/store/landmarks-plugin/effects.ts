import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { of } from 'rxjs';
import { catchError, filter, switchMap, tap } from 'rxjs/operators';
import { LoadingPhotoService } from 'src/app/core/photo-loader/photo-loader.service';
import { getPlugin, getPluginFail, getPluginSuccess } from './action';

@Injectable()
export class LandmarksPlaginEffects {
  constructor(private loaderService: LoadingPhotoService, private actions: Actions, private store: Store<any>) {}

  @Effect({ dispatch: false })
  getPlugin$ = this.actions.pipe(
    ofType(getPlugin),
    switchMap(() =>
      this.loaderService.getPlugin().pipe(
        filter(data => !!data),
        tap(data => {
          const plugins = Object.keys(data.available_plugins);
          const plugin = plugins.find(item => item.includes('landmarks'));
          this.store.dispatch(getPluginSuccess({ landmarks: plugin }));
        }),
        catchError(error => of(getPluginFail(error)))
      )
    )
  );
}
