import {createSelector, createFeatureSelector} from '@ngrx/store';
import * as FromOrganization from './reducers';
import {Organization} from "../../data/organization";
import {EntitySelectorsFactory} from "@ngrx/data";

export const organizationSelectors = new EntitySelectorsFactory().create<any>('Organization');

export const selectOrganizationState = createFeatureSelector<FromOrganization.OrganizationsState>('Organization');

export const selectCurrentOrganizationId = createSelector(
  selectOrganizationState,
  state => state.selectId
);

export const selectSelectedOrganization = createSelector(
  organizationSelectors.selectEntities,
  selectCurrentOrganizationId,
  (organizationCash, selectId) => organizationCash.find(org => org.id === selectId)
);

export const selectUserRollForSelectedOrganization = createSelector(
  selectSelectedOrganization,
  organization => organization ? organization.role : null
);
