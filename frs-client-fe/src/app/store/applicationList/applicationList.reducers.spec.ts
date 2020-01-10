import {
  FetchApplicationList,
  FetchApplicationListSuccess,
  FetchApplicationListFail,
  CreateApplication,
  CreateApplicationSuccess,
  CreateApplicationFail
} from "./action";
import { ApplicationListReducer } from "./reducers";

describe('ApplicationListReducer', () => {
  const initialState = {
    isLoading: false,
    filters: [],
    selectedFilter: null,
    errorMessage: null
  };

  describe('Fetch application actions', () => {
    it('should set loading to true and reset error message', () => {
      const action = new FetchApplicationList({
        organizationId: '0'
      });
      const state = ApplicationListReducer(initialState, action);

      expect(state.isLoading).toBeTruthy();
      expect(state.errorMessage).toBeNull();
    });

    it('should set loading to false', () => {
      const action = new FetchApplicationListSuccess();
      const state = ApplicationListReducer(initialState, action);

      expect(state.isLoading).toBeFalsy();
    });

    it('should update errorMesaage and set loading to false', () => {
      const action = new FetchApplicationListFail({
        errorMessage: 'fetch error'
      });
      const state = ApplicationListReducer(initialState, action);

      expect(state.isLoading).toBeFalsy();
      expect(state.errorMessage).toBe('fetch error');
    });
  });

  describe('Fetch application actions', () => {
    it('should set loading to true and reset error message', () => {
      const action = new FetchApplicationList({
        organizationId: '0'
      });
      const state = ApplicationListReducer(initialState, action);

      expect(state.isLoading).toBeTruthy();
      expect(state.errorMessage).toBeNull();
    });

    it('should set loading to false', () => {
      const action = new FetchApplicationListSuccess();
      const state = ApplicationListReducer(initialState, action);

      expect(state.isLoading).toBeFalsy();
    });

    it('should update errorMesaage and set loading to false', () => {
      const action = new FetchApplicationListFail({
        errorMessage: 'fetch error'
      });
      const state = ApplicationListReducer(initialState, action);

      expect(state.isLoading).toBeFalsy();
      expect(state.errorMessage).toBe('fetch error');
    });
  });

  describe('Create application actions', () => {
    it('should set loading to true', () => {
      const action = new CreateApplication({
        organizationId: '0',
        name: 'test'
      });
      const state = ApplicationListReducer(initialState, action);

      expect(state.isLoading).toBeTruthy();
    });

    it('should set loading to false', () => {
      const action = new CreateApplicationSuccess();
      const state = ApplicationListReducer(initialState, action);

      expect(state.isLoading).toBeFalsy();
    });

    it('should update errorMesaage and set loading to false', () => {
      const action = new CreateApplicationFail({
        errorMessage: 'create app error'
      });
      const state = ApplicationListReducer(initialState, action);

      expect(state.isLoading).toBeFalsy();
      expect(state.errorMessage).toBe('create app error');
    });
  });
});
