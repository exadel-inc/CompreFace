import {createSelector, createFeatureSelector} from '@ngrx/store';
import * as FromOrganization from './reducers';
import {Organization} from "../../data/organization";

//for example
// export const OrganizationSelectors = new EntitySelectorsFactory().create<OrganizationsState>('Organization');

export const selectOrganizationState = createFeatureSelector<FromOrganization.OrganizationsState>('Organization');

export const getSelectOrganizationId = createSelector(
  selectOrganizationState,
  (state) => state.selectId
);
