import {createAction, props} from '@ngrx/store';
import { Application } from 'src/app/data/application';

export const addApplication = createAction('[Application/API] Add Application', props<{ application: Application }>());
export const addApplications = createAction('[Application/API] Add Applications', props<{ applications: Application[] }>());
