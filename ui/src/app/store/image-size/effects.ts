import { Injectable } from '@angular/core';
import { ofType } from '@ngrx/effects';
import { Actions, Effect } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { of } from 'rxjs';
import { catchError, filter, switchMap, tap } from 'rxjs/operators';
import { ImageSizeService } from 'src/app/core/image-size/image-size.service';
import { MaxImageSize } from 'src/app/data/interfaces/size.interface';
import { getMaxImageSize, getMaxImageSizeFail, getMaxImageSizeSuccess } from './actions';

@Injectable()
export class MaxImageSizeEffect {
  constructor(private maxSizeService: ImageSizeService, private actions: Actions, private store: Store<any>) {}

  @Effect({ dispatch: false })
  getMaxSize$ = this.actions.pipe(
    ofType(getMaxImageSize),
    switchMap(() =>
      this.maxSizeService.fetchMaxSize().pipe(
        filter(data => !!data),
        tap((data: MaxImageSize) => this.store.dispatch(getMaxImageSizeSuccess(data))),
        catchError(error => of(getMaxImageSizeFail(error)))
      )
    )
  );
}
