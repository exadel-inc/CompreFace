import {createSelector, createFeatureSelector} from '@ngrx/store';
import * as FromOrganization from './reducers';
import {EntitySelectorsFactory} from "@ngrx/data";
import {selectUserId} from "../userInfo/selectors";

export const organizationSelectors = new EntitySelectorsFactory().create<any>('Organization');

export const selectOrganizationState = createFeatureSelector<FromOrganization.OrganizationsState>('Organization');

export const getSelectedOrganizationId = createSelector(
  selectOrganizationState,
  state => state.selectId
);

export const selectSelectedOrganization = createSelector(
  organizationSelectors.selectEntities,
  getSelectedOrganizationId,
  (organizationCash, selectId) => organizationCash.find(org => org.id === selectId)
);

export const selectUserRollForSelectedOrganization = createSelector(
  selectSelectedOrganization,
  selectUserId,
  (organization, userId) => {
    const role = organization ? organization.userOrganizationRoles.find(role => role.userId === userId) : null;
    return role ? role.role : null;
  }
);
