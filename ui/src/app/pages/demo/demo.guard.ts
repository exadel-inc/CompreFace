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
import { defer, Observable, of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { Store } from '@ngrx/store';
import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { Routes } from '../../data/enums/routers-url.enum';
import { DemoService } from './demo.service';
import { loadDemoApiKeySuccess, setDemoKeyPending } from '../../store/demo/action';
import { DemoStatus } from '../../data/interfaces/demo-status';

@Injectable({
  providedIn: 'root',
})
export class DemoGuard implements CanActivate {
  constructor(private store: Store<any>, private router: Router, private demoService: DemoService) {}

  getModel(): Observable<boolean> {
    return this.demoService.getModel().pipe(
      catchError(() => of(null)),
      map(data => {
        if (data?.apiKey) {
          this.store.dispatch(loadDemoApiKeySuccess(data));
          return true;
        }
        return false;
      })
    );
  }

  canActivate(): Observable<boolean> {
    this.store.dispatch(setDemoKeyPending());

    return this.demoService.getStatus().pipe(
      catchError(() => of(null)),
      map((data: DemoStatus) => data.demoFaceCollectionIsInconsistent),
      switchMap((data: boolean) => defer(() => (data ? of(false) : this.getModel()))),
      tap(data => {
        if (!data) this.router.navigate([Routes.Home]);
      })
    );
  }
}
