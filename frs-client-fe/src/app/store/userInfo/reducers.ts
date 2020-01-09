import {UserInfoActions, UserInfoActionTypes} from './action';

export interface UserInfoState {
  guid: string;
  isAuthenticated: boolean;
  username: string;
}

export const initialState: UserInfoState = {
  isAuthenticated: false,
  username: null,
  guid: null
};


export function UserInfoReducer(state = initialState, action: UserInfoActions): UserInfoState {
  switch(action.type) {
    case UserInfoActionTypes.UPDATE_USER_INFO: {
      return {
        ...state,
        ...action.payload
      };
    }

    case UserInfoActionTypes.RESET_USER_INFO: {
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

    case UserInfoActionTypes.GET_USER_INFO_SUCCESS: {
      return {
        ...state,
        ...action.payload,
      }
    }

    default:
      return state;
  }
}
