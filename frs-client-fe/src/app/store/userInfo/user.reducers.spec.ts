import {UpdateUserAuthorization, ResetUserInfo, UpdateUserInfo} from './action';
import {UserInfoReducer} from './reducers';


describe('UserInfoReducer', () => {
  const initialState = {
    isAuthenticated: false,
    username: null
  };

  describe('UpdateUserAuthorization action', () => {
    it('should set isAuthenticated to true', () => {
      const action = new UpdateUserAuthorization(true);
      const state = UserInfoReducer(initialState, action);

      expect(state.isAuthenticated).toBeTruthy();
    });
  });

  describe('ResetUserInfo action', () => {
    it('should should reset state to initial values', () => {
      const action = new ResetUserInfo();
      const state = UserInfoReducer({
        isAuthenticated: true,
        username: 'testUser'
      }, action);

      expect(state.username).toBeNull();
      expect(state.isAuthenticated).toBeFalsy();
    });
  });

  describe('UpdateUserInfo action', () => {
    it('should update user info according to payload', () => {
      const action = new UpdateUserInfo({
        username: 'myTestUser2',
        isAuthenticated: true
      });
      const state = UserInfoReducer(initialState, action);

      expect(state.isAuthenticated).toBeTruthy();
      expect(state.username).toBe('myTestUser2');
    });
  });
});
