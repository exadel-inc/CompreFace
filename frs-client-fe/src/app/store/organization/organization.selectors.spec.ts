import {
  getSelectedOrganizationId,
  SelectSelectedOrganization,
  SelectUserRollForSelectedOrganization
} from "./selectors";
import {Organization} from "../../data/organization";

describe('OrganizationSelectors', () => {

  it('getSelectedOrganizationId', () => {
    expect(getSelectedOrganizationId.projector({selectId: 'someId'})).toBe('someId');
  });

  it('SelectSelectedOrganization', () => {
    const orgs: Organization[] = [{id: "1", name: 'name1', role: "ADMIN"}, {id: "2", name: 'name2', role: "USER"}];
    expect(SelectSelectedOrganization.projector(orgs, "1")).toEqual({id: "1", name: 'name1', role: "ADMIN"});
    expect(SelectSelectedOrganization.projector(orgs, "2")).toEqual({id: "2", name: 'name2', role: "USER"});
    expect(SelectSelectedOrganization.projector(orgs, "0")).toBeFalsy();
  });

  it('SelectUserRollForSelectedOrganization', () => {
    const org1: Organization = {
      id: "2",
      name: 'name1',
      role: "ADMIN"
    };

    const org2: Organization = {
      id: "2",
      name: 'name1',
      role: "OWNER"
    };
    expect(SelectUserRollForSelectedOrganization.projector(org1)).toBe('ADMIN');
    expect(SelectUserRollForSelectedOrganization.projector(org2)).toBe('OWNER');
    expect(SelectUserRollForSelectedOrganization.projector(null)).toBeFalsy();
  });
});
