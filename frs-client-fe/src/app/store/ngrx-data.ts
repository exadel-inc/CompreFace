
import {
  EntityMetadataMap,
  DefaultDataServiceConfig
} from 'ngrx-data';
import {environment} from "../../environments/environment";

export const defaultDataServiceConfig: DefaultDataServiceConfig = {
  root: environment.apiUrl
};

export const entityMetadata: EntityMetadataMap = {
  Organization: {},
  User:{}
};

export const pluralNames = { Organization: 'organizations'};

