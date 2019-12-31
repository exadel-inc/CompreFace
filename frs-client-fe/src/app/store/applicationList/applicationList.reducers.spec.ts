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

  const mockApplications = [{
    "id": "0",
    "name": "Application 0",
    "owner": {
      "id": "0",
      "firstName": "Owner 0",
      "lastName": "lastname owner 0"
    }
  },
  {
    "id": "1",
    "organizationId": "0",
    "owner": {
      "id": "1",
      "firstName": "Owner 1",
      "lastName": "lastname owner 1"
    }
  }];

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
