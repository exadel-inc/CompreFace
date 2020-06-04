import {updateUserAuthorization, resetUserInfo, updateUserInfo} from './action';
import {UserInfoReducer} from './reducers';


describe('UserInfoReducer', () => {
  const initialState = {
    isAuthenticated: false,
    avatar: '',
    email: '',
    firstName: '',
    guid: '',
    userId: '',
    lastName: '',
    password: ''
  };

  describe('UpdateUserAuthorization action', () => {
    it('should set isAuthenticated to true', () => {
      const action = updateUserAuthorization({ value: true });
      const state = UserInfoReducer(initialState, action);

      expect(state.isAuthenticated).toBeTruthy();
    });
  });

  describe('ResetUserInfo action', () => {
    it('should should reset state to initial values', () => {
      const action = resetUserInfo();
      const state = UserInfoReducer({
        isAuthenticated: true,
        avatar: '',
        email: '',
        firstName: '',
        guid: '',
        userId: '',
        lastName: '',
        password: ''
      }, action);

      expect(state.firstName).toBeNull();
      expect(state.isAuthenticated).toBeFalsy();
    });
  });

  describe('UpdateUserInfo action', () => {
    it('should update user info according to payload', () => {
      const action = updateUserInfo({
        firstName: 'myTestUser2',
        isAuthenticated: true
      });
      const state = UserInfoReducer(initialState, action);

      expect(state.isAuthenticated).toBeTruthy();
      expect(state.firstName).toBe('myTestUser2');
    });
  });
});
