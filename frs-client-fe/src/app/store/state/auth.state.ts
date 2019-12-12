import {User} from "../../data/user";

export interface State {
  isAuthenticated: boolean;
  user: User | null;
  errorMessage: string | null;
  successMessage: string | null;
  isLoading: boolean;
}

export const initialState: State = {
  isAuthenticated: false,
  user: null,
  errorMessage: null,
  successMessage: null,
  isLoading: false,
};
