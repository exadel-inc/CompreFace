import {
  getSelectedOrganizationId,
  SelectSelectedOrganization,
  SelectUserRollForSelectedOrganization
} from "./selectors";

describe('OrganizationSelectors', () => {

  it('getSelectedOrganizationId', () => {
    expect(getSelectedOrganizationId.projector({selectId: 'someId'})).toBe('someId');
  });

  it('SelectSelectedOrganization', () => {
    const org = [{id: 1, name: 'name1'}, {id: 2, name: 'name2'}];
    expect(SelectSelectedOrganization.projector(org, 1)).toEqual({id: 1, name: 'name1'});
    expect(SelectSelectedOrganization.projector(org, 2)).toEqual({id: 2, name: 'name2'});
    expect(SelectSelectedOrganization.projector(org, 0)).toBeFalsy();
  });

  it('SelectUserRollForSelectedOrganization', () => {
    const org = {
      id: 2,
      name: 'name1',
      userOrganizationRoles: [
        {userId: 1, role: 'ADMIN'},
        {userId: 2, role: 'OWNER'},
      ]
    };
    expect(SelectUserRollForSelectedOrganization.projector(org, 1)).toBe('ADMIN');
    expect(SelectUserRollForSelectedOrganization.projector(org, 2)).toBe('OWNER');
    expect(SelectUserRollForSelectedOrganization.projector(org, 0)).toBeFalsy();
  });
});
