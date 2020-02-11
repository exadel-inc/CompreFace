import {createReducer, on} from '@ngrx/store';
import {EntityState, createEntityAdapter} from '@ngrx/entity';
import {AppUser} from 'src/app/data/appUser';
import {
  loadAppUserEntityAction,
  addAppUserEntityAction,
  putUpdatedAppUserRoleEntityAction,
  updateUserRoleEntityAction
} from './actions';

export interface AppUserEntityState extends EntityState<AppUser> {
  isPending: boolean;
}

export const appUserAdapter = createEntityAdapter<AppUser>();

const initialState: AppUserEntityState = appUserAdapter.getInitialState({
  isPending: false
});

export const appUserReducer = createReducer(
  initialState,
  on(loadAppUserEntityAction, (state) => ({ ...state, isPending: true })),
  on(addAppUserEntityAction, (state, { users }) => {
    const newState = { ...state, isPending: false };
    return appUserAdapter.addAll(users, newState);
  }),
  on(putUpdatedAppUserRoleEntityAction, (state) => ({ ...state, isPending: true })),
  on(updateUserRoleEntityAction, (state, { user }) => {
    const newState = { ...state, isPending: false };
    return appUserAdapter.updateOne({
      id: user.id,
      changes: {
        accessLevel: user.accessLevel
      }
    }, newState);
  })
);
