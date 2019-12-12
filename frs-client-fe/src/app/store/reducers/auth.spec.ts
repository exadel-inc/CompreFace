import {initialState} from '../state/auth.state';
import * as authReducers from './auth';
import {LogInSuccess, LogInFailure, SignUpFailure, SignUpSuccess, LogOut} from "../actions/auth";


describe('AuthReducer', () => {

  describe('LOGIN_SUCCESS action', () => {
    it('should set isAuthenticated to true, and loading false', () => {
      const { reducer } = authReducers;
      const action = new LogInSuccess({});
      const state = reducer(initialState, action);

      expect(state.isAuthenticated).toBe(true);
      expect(state.errorMessage).toBe(null);
      expect(state.isLoading).toBe(false);
    });
  });

  describe('LOGIN_FAILURE action', () => {
    it('should set errorMessage to value, and loading false', () => {
      const { reducer } = authReducers;
      const action = new LogInFailure({});
      const state = reducer(initialState, action);
      expect(state.isAuthenticated).toBe(false);
      expect(state.errorMessage).toBe('Incorrect email and/or password.');
      expect(state.isLoading).toBe(false);
    });
  });

  describe('SIGNUP_SUCCESS action', () => {
    it('should set successMessage to value, and loading false', () => {
      const { reducer } = authReducers;
      const action = new SignUpSuccess({});
      const state = reducer(initialState, action);

      expect(state.successMessage).toBe('You have created new account, please login into your account');
      expect(state.errorMessage).toBe(null);
      expect(state.isLoading).toBe(false);
    });
  });


  describe('SIGNUP_FAILURE action', () => {
    it('should set errorMessage to value, and loading false', () => {
      const { reducer } = authReducers;
      const action = new SignUpFailure({});
      const state = reducer(initialState, action);

      expect(state.errorMessage).toBe('That email is already in use.');
      expect(state.successMessage).toBe(null);
      expect(state.isAuthenticated).toBe(false);
      expect(state.isLoading).toBe(false);
    });
  });
});
