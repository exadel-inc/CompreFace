import {createAction, props} from '@ngrx/store';

export const getSelectedId = createAction('[Organization] Get Selected Id');
export const setSelectedId = createAction('[Organization] Set Selected Id', props<{selectId: string}>());
