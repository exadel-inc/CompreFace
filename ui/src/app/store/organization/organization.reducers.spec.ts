import {OrganizationReducer} from './reducers';
import {setSelectedId} from './action';

describe('OrganizationReducer', () => {
  const initialState = {selectId: null};

  it('should set selectId', () => {
    const action = setSelectedId({selectId: 'someId'});
    const state = OrganizationReducer(initialState, action);
    expect(state.selectId).toBe('someId');
  });
});
