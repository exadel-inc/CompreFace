import {
  selectCurrentOrganizationId,
  selectSelectedOrganization,
  selectUserRollForSelectedOrganization
} from './selectors';
import {Organization} from '../../data/organization';

describe('OrganizationSelectors', () => {

  it('selectCurrentOrganizationId', () => {
    expect(selectCurrentOrganizationId.projector({selectId: 'someId'})).toBe('someId');
  });

  it('SelectSelectedOrganization', () => {
    const org = [{id: 1, name: 'name1'}, {id: 2, name: 'name2'}];
    expect(selectSelectedOrganization.projector(org, 1)).toEqual({id: 1, name: 'name1'});
    expect(selectSelectedOrganization.projector(org, 2)).toEqual({id: 2, name: 'name2'});
    expect(selectSelectedOrganization.projector(org, 0)).toBeFalsy();
  });

  it('SelectUserRollForSelectedOrganization', () => {
    const org1: Organization = {
      id: '2',
      name: 'name1',
      role: 'ADMIN'
    };

    const org2: Organization = {
      id: '2',
      name: 'name1',
      role: 'OWNER'
    };
    expect(selectUserRollForSelectedOrganization.projector(org1)).toBe('ADMIN');
    expect(selectUserRollForSelectedOrganization.projector(org2)).toBe('OWNER');
    expect(selectUserRollForSelectedOrganization.projector(null)).toBeFalsy();
  });
});
