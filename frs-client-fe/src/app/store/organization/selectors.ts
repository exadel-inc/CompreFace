import {createSelector, createFeatureSelector} from '@ngrx/store';
import * as FromOrganization from './reducers';

//for example
// export const OrganizationSelectors = new EntitySelectorsFactory().create<OrganizationsState>('Organization');

export const selectOrganizationState = createFeatureSelector<FromOrganization.OrganizationsState>('Organization');

export const getSelectedOrganizationId = createSelector(
  selectOrganizationState,
  (state) => state.selectId
);
