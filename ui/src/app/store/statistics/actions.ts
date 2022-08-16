import { createAction, props } from '@ngrx/store';
import { Statistics } from 'src/app/data/interfaces/statistics';

export const loadModelStatistics = createAction('[Model] Load Model Statistics', props<{ appId: string; modelId: string }>());
export const loadModelStatisticsSuccess = createAction('[Model] Load Model Statistics Success', props<Statistics>());
export const loadModelStatisticsFail = createAction('[Model] Load Model Statistics Fail', props<{ error: any }>());
