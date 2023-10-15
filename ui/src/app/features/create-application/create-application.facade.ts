import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { Application } from 'src/app/data/interfaces/application';
import { AppState } from 'src/app/store';
import { createApplication } from 'src/app/store/application/action';
import { selectApplications } from 'src/app/store/application/selectors';
import { createModel } from 'src/app/store/model/action';

@Injectable()
export class CreateApplicationFacade {
  applications$: Observable<Application[]>;

  constructor(private store: Store<AppState>) {
    this.applications$ = this.store.select(selectApplications);
  }

  createApplication(name: string) {
    this.store.dispatch(createApplication({ name }));
  }

  createModel(name: string, type: string, applicationId: string, isFirstService = false): void {
    this.store.dispatch(
      createModel({
        model: {
          applicationId,
          name,
          type,
          isFirstService
        }
      })
    );
  }
}
