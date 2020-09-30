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
import { of } from 'rxjs';
import {Store} from '@ngrx/store';
import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import {OrganizationEnService} from '../organization/organization-entitys.service';
import {AppState} from '../index';
import { SnackBarService } from '../../features/snackbar/snackbar.service';
import { loadOrganizations, loadOrganizationsFail, loadOrganizationsSuccess } from './action';

@Injectable()
export class OrganizationEffect {
    constructor(
        private actions: Actions,
        private organizationEnService: OrganizationEnService,
        private snackBarService: SnackBarService,
        private store: Store<AppState>
    ) {
    }

    @Effect()
    fetchOrganizations$ =
        this.actions.pipe(
            ofType(loadOrganizations),
            switchMap((action) => this.organizationEnService.getAll().pipe(
              map(loadOrganizationsSuccess)
            )),
            catchError(error => of(loadOrganizationsFail({ error })))
        );

    @Effect({ dispatch: false })
    showError$ =
        this.actions.pipe(
            ofType(loadOrganizationsFail),
            tap(action => {
                this.snackBarService.openHttpError(action.error);
            })
        );
}
