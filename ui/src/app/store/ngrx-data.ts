/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import {EntityMetadataMap, EntityDataModuleConfig, DefaultDataServiceConfig} from '@ngrx/data';
import {environment} from '../../environments/environment';

export const defaultDataServiceConfig: DefaultDataServiceConfig = {
  // root: environment.apiUrl,
  // example of configuration:
  entityHttpResourceUrls: {
    // Case matters. Match the case of the entity name.
    Organization: {
      // You must specify the root as part of the resource URL.
      entityResourceUrl: environment.apiUrl + 'org/',
      collectionResourceUrl: environment.apiUrl + 'orgs'
    }
  },
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
