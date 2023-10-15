import { Action, ActionReducer, on, createReducer } from '@ngrx/store';
import { Plugin } from 'src/app/data/interfaces/plugins';
import { getPlugin, getPluginFail, getPluginSuccess } from './action';

const defaultLandmarksPlugin: Plugin = {
  landmarks: 'landmarks',
};

const reducer: ActionReducer<Plugin> = createReducer(
  defaultLandmarksPlugin,
  on(getPlugin, () => ({ ...defaultLandmarksPlugin })),
  on(getPluginSuccess, (state, action) => ({ ...state, ...action })),
  on(getPluginFail, state => ({ ...state }))
);

export const pluginsReducer = (plugin: Plugin, action: Action) => reducer(plugin, action);
