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

import { NgModule } from '@angular/core';
import { DefaultDataServiceConfig, EntityDataModule } from '@ngrx/data';
import { EffectsModule } from '@ngrx/effects';
import { RouterStateSerializer, StoreRouterConnectingModule } from '@ngrx/router-store';
import { StoreModule } from '@ngrx/store';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';

import { sharedReducers } from '.';
import { environment } from '../../environments/environment';
import { AppUserStoreModule } from './app-user/app-user.module';
import { ApplicationStoreModule } from './application/application.module';
import { AuthEffects } from './auth/effects';
import { ModelStoreModule } from './model/model.module';
import { defaultDataServiceConfig, entityConfig } from './ngrx-data';
import { OrganizationStoreModule } from './organization/organization.module';
import { RoleStoreModule } from './role/role.module';
import { AppSerializer } from './router/reducer';
import { UserStoreModule } from './user/user.module';
import { UserInfoStoreModule } from './userInfo/user-info.module';
import { TestModelStoreModule } from './test-model/test-model.module';

@NgModule({
  declarations: [],
  imports: [
    StoreModule.forRoot(sharedReducers),
    EffectsModule.forRoot([AuthEffects]),
    EntityDataModule.forRoot(entityConfig),
    OrganizationStoreModule,
    UserInfoStoreModule,
    ApplicationStoreModule,
    UserStoreModule,
    RoleStoreModule,
    ModelStoreModule,
    TestModelStoreModule,
    AppUserStoreModule,
    StoreRouterConnectingModule.forRoot({
      stateKey: 'router'
    }),
    !environment.production
      ? StoreDevtoolsModule.instrument({ maxAge: 30 })
      : [],
  ],
  providers: [
    { provide: DefaultDataServiceConfig, useValue: defaultDataServiceConfig },
    { provide: RouterStateSerializer, useClass: AppSerializer },
  ]
})
export class AppStoreModule { }
