import {State} from "../reducers/auth";

export const initialState: State = {
  isAuthenticated: false,
  user: null,
  errorMessage: null
};
