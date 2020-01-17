import {
  FetchUsers,
  FetchUsersSuccess,
  FetchUsersFail,
  UpdateUserRole,
  UpdateUserRoleSuccess,
  UpdateUserRoleFail,
  InviteUser,
  InviteUserSuccess,
  InviteUserFail
} from './actions';
import { createReducer, on, ActionReducer } from '@ngrx/store';

export interface UserListState {
  isLoading: boolean;
  filters: any[];
  selectedFilter: string;
  errorMessage: string;
  searchTerm: string;
  invitedEmail: string;
}

const initialState: UserListState = {
  isLoading: false,
  filters: [],
  selectedFilter: null,
  errorMessage: null,
  searchTerm: null,
  invitedEmail: null
}

export const UserListReducer: ActionReducer<UserListState> = createReducer(
  initialState,
  on(FetchUsers, (state) => ({
    ...state,
    invitedEmail: null,
    errorMessage: null,
    isLoading: true
  })),
  on(FetchUsersSuccess, (state) => ({
    ...state,
    isLoading: false
  })),
  on(FetchUsersFail, (state, { errorMessage }) => ({
    ...state,
    errorMessage,
    isLoading: false
  })),
  on(UpdateUserRole, (state) => ({
    ...state,
    invitedEmail: null,
    errorMessage: null,
    isLoading: true
  })),
  on(UpdateUserRoleSuccess, (state) => ({
    ...state,
    isLoading: false
  })),
  on(UpdateUserRoleFail, (state, { errorMessage }) => ({
    ...state,
    errorMessage
  })),
  on(InviteUser, (state) => ({
    ...state,
    invitedEmail: null,
    errorMessage: null
  })),
  on(InviteUserSuccess, (state, { userEmail }) => ({
    ...state,
    invitedEmail: userEmail
  })),
  on(InviteUserFail, (state, { errorMessage }) => ({
    ...state,
    errorMessage
  }))
);

    case UserListActionTypes.FETCH_AVAILABLE_USER_ROLES: {
      return {
        ...state,
        isLoading: true
      }
    }

    case UserListActionTypes.FETCH_AVAILABLE_USER_ROLES_SUCCESS: {
      return {
        ...state,
        isLoading: false
      }
    }

    case UserListActionTypes.FETCH_AVAILABLE_USER_ROLES_FAIL: {
      return {
        ...state,
        isLoading: false,
