import {createAction, props} from '@ngrx/store';
import { Application } from 'src/app/data/application';

export const addApplication = createAction('[Application/API] Add Application', props<{ application: Application }>());
export const addApplications = createAction('[Application/API] Add Applications', props<{ applications: Application[] }>());
export const updateApplication = createAction('[Application/API] Update Applications', props<{ application: Application }>());
export const setSelectedId = createAction('[Application/API] Set Selected Id Applications', props<{ selectedAppId }>());
