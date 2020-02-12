import {NgModule} from '@angular/core';
import {sharedReducers} from './index';
import {EffectsModule} from '@ngrx/effects';
import {AuthEffects} from './auth/effects';
import {DefaultDataServiceConfig, EntityDataModule} from '@ngrx/data';
import {defaultDataServiceConfig, entityConfig} from './ngrx-data';
import {OrganizationStoreModule} from './organization/organization.module';
import {UserInfoStoreModule} from './userInfo/user-info.module';
import {ApplicationStoreModule} from './application/application.module';
import {UserStoreModule} from './user/user.module';
import {RoleStoreModule} from './role/role.module';
import {ModelStoreModule} from './model/model.module';
import {AppUserStoreModule} from './app-user/app-user.module';
import {ModelRelationStoreModule} from './model-relation/model-relation.module';
import {StoreModule} from '@ngrx/store';
import {RouterStateSerializer, StoreRouterConnectingModule} from '@ngrx/router-store';
import {AppSerializer} from './router/reducer';
import {environment} from '../../environments/environment';
import {StoreDevtoolsModule} from '@ngrx/store-devtools';

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
    ModelRelationStoreModule,
    StoreRouterConnectingModule.forRoot({
      stateKey: 'router'
    }),
    !environment.production
      ? StoreDevtoolsModule.instrument({maxAge: 30})
      : [],
  ],
  providers: [
    {provide: DefaultDataServiceConfig, useValue: defaultDataServiceConfig},
    {provide: RouterStateSerializer, useClass: AppSerializer},
  ]
})
export class AppStoreModule { }
