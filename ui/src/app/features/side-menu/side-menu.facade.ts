import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { CollectionItem } from 'src/app/data/interfaces/collection';
import { AppState } from 'src/app/store';
import { selectImageCollection } from 'src/app/store/manage-collectiom/selectors';

@Injectable()
export class SideMenuFacade {
  collectionItems$: Observable<CollectionItem[]>;

  constructor(private store: Store<AppState>) {
    this.collectionItems$ = this.store.select(selectImageCollection);
  }
}
