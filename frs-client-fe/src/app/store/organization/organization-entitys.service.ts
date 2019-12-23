import {
  EntityCollectionServiceBase,
  EntityCollectionServiceElementsFactory
} from '@ngrx/data';
import {Organization} from "../../data/organization";
import {Injectable} from "@angular/core";

@Injectable()
export class OrganizationEnService extends EntityCollectionServiceBase<Organization> {
  constructor(serviceElementsFactory: EntityCollectionServiceElementsFactory) {
    // console.log('serviceElementsFactory', serviceElementsFactory);
    super('Organization', serviceElementsFactory);
  }
}

