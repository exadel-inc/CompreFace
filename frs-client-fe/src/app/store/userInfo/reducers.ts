import { UserInfoActions, UserInfoActionTypes } from './action';

export interface UserInfoState {
  isAuthenticated: boolean;
  username: string;
}

export const initialState: UserInfoState = {
  isAuthenticated: false,
  username: null
};


export function UserInfoReducer(state = initialState, action: UserInfoActions): UserInfoState {
  switch(action.type) {
    case UserInfoActionTypes.UPDATE_USERINFO: {
      return {
        ...state,
        ...action.payload
      };
    }

    case UserInfoActionTypes.RESET_USERINFO: {
      return {
        ...initialState
      };
    }

    case UserInfoActionTypes.UPDATE_AUTHORIZED: {
      return {
        ...state,
        isAuthenticated: action.payload
      }
    }

    default:
      return state;
  }
}