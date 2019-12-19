import {
  createSelector,
  createFeatureSelector,
  ActionReducerMap,
} from '@ngrx/store';
import * as FromOrganization from './reducers';
import {EntitySelectorsFactory} from "@ngrx/data";
import {Organization} from "../../data/organization";
import {OrganizationsState} from "./reducers";

export const OrganizationSelectors = new EntitySelectorsFactory().create<OrganizationsState>('Organization');

// export const reducers: ActionReducerMap<OrganizationsState> = FromOrganization.OrganizationReducer;

export const selectOrganizationState = createFeatureSelector<FromOrganization.OrganizationsState>('Organization');

// export const selectOrganizationIds = createSelector(
//   selectOrganizationState,
//   FromOrganization.selectOrganizationIds // shorthand for OrganizationsState => FromOrganization.selectOrganizationIds(OrganizationsState)
// );

export const getSelectedOrganization = createSelector(
  selectOrganizationState,
  OrganizationSelectors.selectEntities,
  (organizationState, ownerEntities) => ownerEntities[organizationState.selectId]
);

export const getSelectOrganizationId = createSelector(
  selectOrganizationState,
  (state) => state.selectId
);

// export const selectCurrentOrganization = createSelector(
//   selectOrganizationEntities,
//   selectCurrentOrganizationId,
//   (OrganizationEntities, OrganizationId) => OrganizationEntities[OrganizationId]
// );


