import {createAction, props} from '@ngrx/store';
import { Application } from 'src/app/data/application';

export const loadApplicationsEntityAction = createAction('[Application/API] Load Applications', props<{ organizationId: string }>());
export const addApplicationEntityAction = createAction('[Application/API] Add Application', props<{ application: Application }>());
export const addApplicationsEntityAction = createAction('[Application/API] Add Applications', props<{ applications: Application[] }>());
export const putUpdatedApplicationEntityAction = createAction('[Application/API] Put Updated Application', props<{ application: Application }>());
export const updateApplicationEntityAction = createAction('[Application/API] Update Application', props<{ application: Application }>());
export const setSelectedIdEntityAction = createAction('[Application/API] Set Selected Id Applications', props<{ selectedAppId }>());
export const createApplicationEntityAction = createAction('[Application/API] Create Application', props<{ organizationId: string, name: string }>());
