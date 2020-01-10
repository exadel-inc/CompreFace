import { AppUser } from 'src/app/data/appUser';
import { UserEntityActionList, UserEntityActionType } from './action';

import { EntityState, createEntityAdapter, EntityAdapter } from '@ngrx/entity';

export const userAdapter: EntityAdapter<AppUser> = createEntityAdapter<AppUser>();
const initialState: EntityState<AppUser> = userAdapter.getInitialState();

export function AppUserReducer(state = initialState, action: UserEntityActionType): EntityState<AppUser> {
    switch(action.type) {
        case UserEntityActionList.ADD_USERS: {
            return userAdapter.addAll(action.payload.users, state);
        }

        case UserEntityActionList.UPDATE_ROLE: {
            return userAdapter.updateOne({
                id: action.payload.user.id,
                changes: {
                    accessLevel: action.payload.user.accessLevel
                }
            }, state)
        }

        default: {
            return state
        }
    }
}
