import { createFeatureSelector, createSelector } from '@ngrx/store';
import { MaxImageSize } from 'src/app/data/interfaces/size.interface';

export const selectMaxSizeState = createFeatureSelector('maxFileSize');
export const selectMaxFileSize = createSelector(selectMaxSizeState, (state: MaxImageSize) => state);
