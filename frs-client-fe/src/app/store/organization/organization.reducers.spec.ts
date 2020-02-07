import {OrganizationReducer} from './reducers';
import {SetSelectedId} from './action';

describe('OrganizationReducer', () => {
  const initialState = {selectId: null};

  it('should set selectId', () => {
    const action = new SetSelectedId({selectId: 'someId'});
    const state = OrganizationReducer(initialState, action);
    expect(state.selectId).toBe('someId');
  });
});
