import { ApplicationListActions, ApplicationListTypes } from './action';

export interface ApplicationListState {
  isLoading: boolean;
  filters: any[];
  selectedFilter: string;
  applicationList: any[];
  errorMessage: string;
};

export const initialState: ApplicationListState = {
  isLoading: false,
  filters: [],
  selectedFilter: null,
  applicationList: [],
  errorMessage: null
}

export function ApplicationListReducer(state = initialState, action: ApplicationListActions): ApplicationListState {
  switch (action.type) {
    case ApplicationListTypes.FETCH_APPLICATION: {
      return {
        ...state,
        isLoading: true
      };
    }

    case ApplicationListTypes.FETCH_APPLICATION_SUCCESS: {
      return {
        ...state,
        applicationList: action.payload.applicationList,
        isLoading: false
      }
    }

    case ApplicationListTypes.FETCH_AFETCH_APPLICATION_FAIL: {
      return {
        ...state,
        applicationList: action.payload.applicationList,
        errorMessage: action.payload.errorMessage,
        isLoading: false
      }
    }

    default: {
      return state;
    }
  }
}
