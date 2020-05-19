import {selectUserId} from './selectors';

describe('UserSelectors', () => {
  it('selectUserId', () => {
    expect(selectUserId.projector({userId: 'someId'})).toBe('someId');
  });
});
