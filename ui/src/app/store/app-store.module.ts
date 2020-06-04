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
