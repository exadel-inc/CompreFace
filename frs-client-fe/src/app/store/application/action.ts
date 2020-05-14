import { createAction, props } from '@ngrx/store';
import { Application, ApplicationUpdateDto } from 'src/app/data/application';

export const loadApplications = createAction('[Application] Load Applications', props<{ organizationId: string }>());
export const loadApplicationsSuccess = createAction('[Application] Load Applications Success', props<{ applications: Application[] }>());
export const loadApplicationsFail = createAction('[Application] Load Applications Fail', props<{ error: any }>());

export const createApplication = createAction('[Application] Create Application', props<{ organizationId: string, name: string }>());
export const createApplicationSuccess = createAction('[Application] Create Application Success', props<{ application: Application }>());
export const createApplicationFail = createAction('[Application] Create Application Fail', props<{ error: any }>());

export const updateApplication = createAction('[Application] Update Application', props<ApplicationUpdateDto>());
export const updateApplicationSuccess = createAction('[Application] Update Application Success', props<{ application: Application }>());
export const updateApplicationFail = createAction('[Application] Update Application Fail', props<{ error: any }>());

export const setSelectedIdEntityAction = createAction('[Application] Set Selected Id Applications', props<{ selectedAppId }>());

