import {createFeatureSelector} from '@ngrx/store';
import {AppState} from '../index';

export const selectAuthState = createFeatureSelector<AppState>('auth');
