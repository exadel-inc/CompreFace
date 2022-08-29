import { Action } from '@ngrx/store';
import { ActionReducer, createReducer, on } from '@ngrx/store';
import { MaxImageSize } from 'src/app/data/interfaces/size.interface';
import { getMaxImageSize, getMaxImageSizeFail, getMaxImageSizeSuccess } from './actions';

const initialState: MaxImageSize = {
  clientMaxFileSize: null,
  clientMaxBodySize: null,
};

const reducer: ActionReducer<MaxImageSize> = createReducer(
  initialState,
  on(getMaxImageSize, () => ({ ...initialState })),
  on(getMaxImageSizeSuccess, (state, action) => ({ ...state, ...action })),
  on(getMaxImageSizeFail, state => ({ ...state }))
);

export const maxSizeReducer = (maxSizeState: MaxImageSize, action: Action) => reducer(maxSizeState, action);
