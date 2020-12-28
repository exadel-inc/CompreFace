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
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Store } from '@ngrx/store';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Routes } from '../../data/enums/routers-url.enum';
import { DemoService } from './demo.service';
import { loadDemoApiKeySuccessAction, setDemoKeyPendingAction } from '../../store/demo/actions';

@Injectable({
  providedIn: 'root',
})
export class DemoGuard implements CanActivate {
  constructor(private store: Store<any>, private router: Router, private demoService: DemoService) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    this.store.dispatch(setDemoKeyPendingAction());

    return this.demoService.getModel().pipe(
      catchError(() => of(null)),
      map(data => {
        if (data?.apiKey) {
          this.store.dispatch(loadDemoApiKeySuccessAction(data));
          return true;
        } else {
          this.router.navigate([Routes.Home]);
          return false;
        }
      })
    );
  }
}
