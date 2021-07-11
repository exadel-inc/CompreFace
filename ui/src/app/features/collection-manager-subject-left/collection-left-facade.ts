import { Injectable } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { addSubject, loadSubjects } from '../../store/manage-collectiom/action';

@Injectable()
export class CollectionLeftFacade {
  private apiKey: string;

  constructor(private route: ActivatedRoute, private store: Store<any>) {}

  initUrlBindingStreams() {
    this.apiKey = this.route.snapshot.queryParams.apiKey;
  }

  addSubject(name: string) {
    this.store.dispatch(addSubject({ name, apiKey: this.apiKey }));
    setTimeout(() => {
      this.store.dispatch(loadSubjects({ apiKey: this.apiKey }));
    }, 500);
  }
}
