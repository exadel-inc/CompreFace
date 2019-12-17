import { Component } from '@angular/core';
import { AuthInit } from './core/auth/auth.init';
import { Store } from '@ngrx/store';
import { AppState } from './store';
import { AuthService } from './core/auth/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.sass']
})
export class AppComponent {
  title = 'frs-client-fe';

  constructor(auth: AuthService, store: Store<AppState>) {
    new AuthInit(store, auth);
  }
}
