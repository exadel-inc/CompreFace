import { NgModule } from '@angular/core';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { MaxImageSizeEffect } from './effects';
import { maxSizeReducer } from './reducers';

@NgModule({
  imports: [EffectsModule.forFeature([MaxImageSizeEffect]), StoreModule.forFeature('maxFileSize', maxSizeReducer)],
})
export class ImageSizeStoreModule {}
