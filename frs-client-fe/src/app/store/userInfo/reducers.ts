import { createReducer, on } from '@ngrx/store';
import {updateUserInfo, resetUserInfo, updateUserAuthorization, getUserInfoSuccess} from './action';

export interface UserInfoState {
  guid?: string;
  isAuthenticated: boolean;
  username: string;
}

export const initialState: UserInfoState = {
  isAuthenticated: false,
  username: null,
  guid: null
};

export const UserInfoReducer = createReducer(initialState,
  on(updateUserInfo, (state, action) => ({
    ...state,
    ...action
  })),
  on(resetUserInfo, () => ({...initialState})),
  on(updateUserAuthorization, (state, action) => ({
    ...state,
    isAuthenticated: action.value
  })),
  on(getUserInfoSuccess, (state, action) => ({
    ...state,
    ...action.user
  })));


// export function UserInfoReducer(state = initialState, action: UserInfoActions): UserInfoState {
//   switch(action.type) {
//     case UserInfoActionTypes.UPDATE_USER_INFO: {
//       return {
//         ...state,
//         ...action.payload
//       };
//     }

//     case UserInfoActionTypes.RESET_USER_INFO: {
//       return {
//         ...initialState
//       };
//     }

//     case UserInfoActionTypes.UPDATE_AUTHORIZED: {
//       return {
//         ...state,
//         isAuthenticated: action.payload
//       }
//     }

//     case UserInfoActionTypes.GET_USER_INFO_SUCCESS: {
//       return {
//         ...state,
//         ...action.payload,
//       };
//     }

//     default:
//       return state;
//   }
// }
