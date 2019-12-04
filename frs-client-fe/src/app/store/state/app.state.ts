import * as auth from '../reducers/auth';

export interface AppState {
  authState: auth.State;
}

export const reducers = {
  auth: auth.reducer
};
