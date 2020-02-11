import {createAction, props} from '@ngrx/store';

export const setSelectedId = createAction('[Organization] Set Selected Id', props<{selectId: string}>());
