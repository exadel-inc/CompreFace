import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {MainLayoutComponent} from './ui/main-layout/main-layout.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {StoreModule} from '@ngrx/store';
import {EffectsModule} from '@ngrx/effects';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {sharedReducers} from './store';
import {ErrorInterceptor, TokenInterceptor} from './core/auth/token.inerceptor';
import {AuthGuard, LoginGuard} from './core/auth/auth.guard';
import {RouterStateSerializer, StoreRouterConnectingModule} from '@ngrx/router-store';
import {environment} from '../environments/environment';
import {StoreDevtoolsModule} from '@ngrx/store-devtools';
import {ToolBarModule} from './features/tool-bar/tool-bar.module';
import {AppSerializer} from './store/router/reducer';
import {AuthEffects} from './store/auth/effects';
import {CreateDialogComponent} from 'src/app/features/create-dialog/create-dialog.component';
import {MatFormFieldModule} from '@angular/material';
import {defaultDataServiceConfig, entityConfig} from './store/ngrx-data';
import {CustomMaterialModule} from './ui/material/material.module';
import {EntityDataModule, DefaultDataServiceConfig} from '@ngrx/data';
import {TableModule} from './features/table/table.module';
import {UserTableModule} from './features/user-table/user-table.module';
import {OrganizationStoreModule} from './store/organization/organization.module';
import {ApplicationStoreModule} from './store/application/application.module';
import {UserInfoStoreModule} from './store/userInfo/user-info.module';
import {ApplicationListModule} from './features/application-list/application-list.module';
import {UserListModule} from './features/user-list/user-list.module';
import {UserStoreModule} from './store/user/user.module';
import {AlertComponent} from './features/alert/alert.component';
import {RoleStoreModule} from './store/role/role.module';
import {ModelListModule} from './features/model-list/model-list.module';
import {ModelStoreModule} from './store/model/model.module';
import {AppUserStoreModule} from './store/app-user/app-user.module';
import {AppUserListModule} from './features/app-user-list/application-user-list.module';
import {ModelRelationStoreModule} from './store/model-relation/model-relation.module';
import {ModelRelationTableModule} from './features/model-relation-table/model-relation-table.module';

@NgModule({
  declarations: [
    AppComponent,
    MainLayoutComponent,
    CreateDialogComponent,
    AlertComponent
  ],
  imports: [
    BrowserModule,
    CustomMaterialModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatFormFieldModule,
    FormsModule,
    ToolBarModule,
    TableModule,
    UserTableModule,
    HttpClientModule,
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
    ApplicationListModule,
    UserListModule,
    ModelListModule,
    AppUserListModule,
    ModelRelationTableModule,
    StoreRouterConnectingModule.forRoot({
      stateKey: 'router'
    }),
    !environment.production
      ? StoreDevtoolsModule.instrument({maxAge: 30})
      : [],
  ],
  providers: [
    {provide: DefaultDataServiceConfig, useValue: defaultDataServiceConfig},
    AuthGuard,
    LoginGuard,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ErrorInterceptor,
      multi: true
    },
    {provide: RouterStateSerializer, useClass: AppSerializer},
  ],
  bootstrap: [AppComponent],
  entryComponents: [CreateDialogComponent, AlertComponent]
})
export class AppModule {
}
