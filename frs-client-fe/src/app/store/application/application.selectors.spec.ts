import {selectCurrentApp, selectCurrentAppId, selectUserRollForSelectedApp} from './selectors';
import {Application} from '../../data/application';

describe('ApplicationSelectors', () => {

  it('selectCurrentAppId', () => {
    expect(selectCurrentAppId.projector({selectedAppId: 'someId'})).toBe('someId');
  });
  it('selectCurrentApp', () => {
    interface AppsInterface {
      entities: Array<Application>;
    }

    const apps: AppsInterface = {
      entities: [
        {
          id: '1',
          name: 'name1',
          owner: {firstName: '', lastName: '', id: ''},
          role: '',
          organizationId: ''
      },
      {
        id: '2',
        name: 'name2',
        owner: {firstName: '', lastName: '', id: ''},
        role: '',
        organizationId: ''
      }
    ]};
    expect(selectCurrentApp.projector(apps, 0)).toEqual(apps.entities[0]);
    expect(selectCurrentApp.projector(apps, 1)).toEqual(apps.entities[1]);
    expect(selectCurrentApp.projector(apps, 3)).toBeFalsy();
  });

  it('SelectUserRollForSelectedApp', () => {
    const app1 = {
      id: 2,
      name: 'name1',
      role: 'ADMIN'
    };

    const app2 = {
      id: 2,
      name: 'name1',
      role: 'OWNER'
    };
    expect(selectUserRollForSelectedApp.projector(app1)).toBe('ADMIN');
    expect(selectUserRollForSelectedApp.projector(app2)).toBe('OWNER');
    expect(selectUserRollForSelectedApp.projector(null)).toBeFalsy();
  });
});
