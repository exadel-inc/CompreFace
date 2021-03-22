import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';
import { Injectable } from '@angular/core';
import { ModelService } from '../model/model.service';
import { catchError, switchMap } from 'rxjs/operators';
import { Observable, of } from 'rxjs';
import { Routes } from '../../data/enums/routers-url.enum';
import { Store } from '@ngrx/store';
import { loadModelsFail } from '../../store/model/actions';

@Injectable({
  providedIn: 'root',
})
export class ApplicationPageGuard implements CanActivate {
  constructor(private store: Store<any>, private router: Router, private modelService: ModelService) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> {
    const { app } = route.queryParams;

    return this.modelService.getAll(app).pipe(
      switchMap(() => of(true)),
      catchError(error => {
        this.router.navigateByUrl(Routes.Home);
        this.store.dispatch(loadModelsFail({ error }));

        return of(false);
      })
    );
  }
}
