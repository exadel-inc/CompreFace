import { ApplicationListActions, ApplicationListTypes } from './action';

export interface ApplicationListState {
  isLoading: boolean;
  filters: any[];
  selectedFilter: string;
  errorMessage: string;
};

export const initialState: ApplicationListState = {
  isLoading: false,
  filters: [],
  selectedFilter: null,
  errorMessage: null
}

export function ApplicationListReducer(state = initialState, action: ApplicationListActions): ApplicationListState {
  switch (action.type) {
    case ApplicationListTypes.FETCH_APPLICATION: {
      return {
        ...state,
        errorMessage: null,
        isLoading: true
      };
    }

    case ApplicationListTypes.FETCH_APPLICATION_SUCCESS: {
      return {
        ...state,
        isLoading: false
      }
    }

    case ApplicationListTypes.FETCH_APPLICATION_FAIL: {
      return {
        ...state,
        errorMessage: action.payload.errorMessage,
        isLoading: false
      }
    }

    case ApplicationListTypes.CREATE_APPLICATION: {
      return {
        ...state,
        isLoading: true
      }
    }

    case ApplicationListTypes.CREATE_APPLICATION_SUCCESS: {
      return {
        ...state,
        isLoading: false
      }
    }

    case ApplicationListTypes.CREATE_APPLICATION_FAIL: {
      return {
        ...state,
        errorMessage: action.payload.errorMessage,
        isLoading: false
      }
    }

    default: {
      return state;
    }
  }
}
