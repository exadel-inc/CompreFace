import { ActionReducer, createReducer, on, Action } from '@ngrx/store';
import { MaxImageSize } from 'src/app/data/interfaces/size.interface';
import { getMaxImageSize, getMaxImageSizeFail, getMaxImageSizeSuccess } from './actions';

const defaultState: MaxImageSize = {
  clientMaxFileSize: 5242880,
  clientMaxBodySize: 10485760,
};

const reducer: ActionReducer<MaxImageSize> = createReducer(
  defaultState,
  on(getMaxImageSize, () => ({ ...defaultState })),
  on(getMaxImageSizeSuccess, (state, action) => ({ ...state, ...action })),
  on(getMaxImageSizeFail, state => ({ ...state }))
);

export const maxSizeReducer = (maxSizeState: MaxImageSize, action: Action) => reducer(maxSizeState, action);
