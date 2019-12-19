import { Injectable } from '@angular/core';
import {
  EntityCollectionServiceBase,
  EntityCollectionServiceElementsFactory
} from '@ngrx/data';
import {Organization} from "../../data/organization";

@Injectable({ providedIn: 'root' })
export class OrganizationEnService extends EntityCollectionServiceBase<Organization> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    console.log('serviceElementsFactory', serviceElementsFactory);
    super('Organization', serviceElementsFactory);
  }

}

