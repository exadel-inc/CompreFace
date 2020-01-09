import {createSelector, createFeatureSelector} from '@ngrx/store';
import * as FromOrganization from './reducers';
import {Organization} from "../../data/organization";
import {EntitySelectorsFactory} from "@ngrx/data";
import {selectUserId} from "../userInfo/selectors";

export const OrganizationSelectors = new EntitySelectorsFactory().create<any>('Organization');

export const selectOrganizationState = createFeatureSelector<FromOrganization.OrganizationsState>('Organization');

export const SelectOrganizationId = createSelector(
  selectOrganizationState,
  (state) => state.selectId
);

export const SelectSelectedOrganization = createSelector(
  OrganizationSelectors.selectEntities,
  SelectOrganizationId,
  (organizationCash, selectId) => organizationCash.find(org => org.id === selectId)
);

export const SelectUserRollForSelectedOrganization = createSelector(
  SelectSelectedOrganization,
  selectUserId,
  (organization, userId) => {
    const role = organization ? organization.userOrganizationRoles.find(role => role.userId === userId) : null;
    return role ? role.role : null;
  }
);
