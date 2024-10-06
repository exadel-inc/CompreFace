import { createAction, props } from '@ngrx/store';
import { MaxImageSize } from 'src/app/data/interfaces/size.interface';

export const getMaxImageSize = createAction('[Application] Get max image & files size');
export const getMaxImageSizeSuccess = createAction('[Application] Get max image size success', props<MaxImageSize>());
export const getMaxImageSizeFail = createAction('[Application] Get max image size fail', props<{ error: any }>());
