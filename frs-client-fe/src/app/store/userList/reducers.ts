import { UserListActions, UserListActionTypes } from './actions';

export interface UserListState {
  isLoading: boolean;
  filters: any[];
  selectedFilter: string;
  errorMessage: string;
  searchTerm: string;
}

const initialState: UserListState = {
  isLoading: false,
  filters: [],
  selectedFilter: null,
  errorMessage: null,
  searchTerm: null
}

export function UserListReducer(state = initialState, action: UserListActions): UserListState {
  switch (action.type) {
    case UserListActionTypes.FETCH_USERS: {
      return {
        ...state,
        errorMessage: null,
        isLoading: true
      }
    }

    case UserListActionTypes.FETCH_USERS_SUCCESS: {
      return {
        ...state,
        isLoading: false
      }
    }

    case UserListActionTypes.FETCH_USERS_FAIL: {
      return {
        ...state,
        errorMessage: action.payload.errorMessage
      }
    }

    case UserListActionTypes.UPDATE_USER_ROLE: {
      return {
        ...state,
        errorMessage: null,
        isLoading: true
      }
    }

    case UserListActionTypes.UPDATE_USER_ROLE_SUCCESS: {
      return {
        ...state,
        isLoading: false
      }
    }

    case UserListActionTypes.UPDATE_USER_ROLE_FAIL: {
      return {
        ...state,
        errorMessage: action.payload.errorMessage
      }
    }

    default: {
      return state;
    }
  }
}
