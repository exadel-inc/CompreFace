import { EntityMetadataMap, EntityDataModuleConfig, DefaultDataServiceConfig } from '@ngrx/data';
import {environment} from "../../environments/environment";

export const defaultDataServiceConfig: DefaultDataServiceConfig = {
  root: environment.apiUrl,
  // example of configuration:
  // entityHttpResourceUrls: {
  //   // Case matters. Match the case of the entity name.
  //   Organization: {
  //     // You must specify the root as part of the resource URL.
  //     entityResourceUrl: 'organizations',
  //     collectionResourceUrl: 'organizations'
  //   }
  // },
};

const entityMetadata: EntityMetadataMap = {
  Organization: {
    additionalCollectionState: {
      selectId: null
    }
  }
};

const pluralNames = {};

export const entityConfig: EntityDataModuleConfig = {
  entityMetadata,
  pluralNames
};
