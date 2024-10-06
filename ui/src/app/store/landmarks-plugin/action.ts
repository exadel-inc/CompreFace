import { createAction, props } from '@ngrx/store';
import { Plugin } from 'src/app/data/interfaces/plugins';

export const getPlugin = createAction('[Application] Get Landmarks Plugin');
export const getPluginSuccess = createAction('[Application] Get Landmarks Plugin Success', props<Plugin>());
export const getPluginFail = createAction('[Application] Get Landmarks Plugin Fail', props<{ error: any }>());
