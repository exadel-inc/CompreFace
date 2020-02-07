import {selectUserId} from './selectors';

describe('UserSelectors', () => {
  it('selectUserId', () => {
    expect(selectUserId.projector({guid: 'someId'})).toBe('someId');
  });
});
