import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {MainLayoutComponent} from './ui/main-layout/main-layout.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {ErrorInterceptor, TokenInterceptor} from './core/auth/token.inerceptor';
import {AuthGuard, LoginGuard} from './core/auth/auth.guard';
import {ToolBarModule} from './features/tool-bar/tool-bar.module';
import {CreateDialogComponent} from 'src/app/features/create-dialog/create-dialog.component';
import {CustomMaterialModule} from './ui/material/material.module';
import {AlertComponent} from './features/alert/alert.component';
import {EditDialogComponent} from './features/edit-dialog/edit-dialog.component';
import {FooterModule} from './features/footer/footer.module';
import {SnackBarModule} from './features/snackbar/snackbar.module';
import {AppStoreModule} from './store/app-store.module';

@NgModule({
  declarations: [
    AppComponent,
    MainLayoutComponent,
    CreateDialogComponent,
    EditDialogComponent,
    AlertComponent
  ],
  imports: [
    BrowserModule,
    CustomMaterialModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    FormsModule,
    ToolBarModule,
    FooterModule,
    AppStoreModule,
    HttpClientModule,
    SnackBarModule,
  ],
  providers: [
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
    }
  ],
  bootstrap: [AppComponent],
  entryComponents: [CreateDialogComponent, AlertComponent, EditDialogComponent]
})
export class AppModule {
}
