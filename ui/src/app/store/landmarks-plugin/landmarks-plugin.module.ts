import { NgModule } from '@angular/core';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { LandmarksPlaginEffects } from './effects';
import { pluginsReducer } from './reducers';

@NgModule({
  imports: [EffectsModule.forFeature([LandmarksPlaginEffects]), StoreModule.forFeature('landmarksPlugin', pluginsReducer)],
})
export class LandmarksPluginModule {}
