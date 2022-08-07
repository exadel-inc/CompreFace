import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ChartsModule } from 'ng2-charts';
import { ModelStatisticsComponent } from './model-statistics.component';

@NgModule({
  declarations: [ModelStatisticsComponent],
  imports: [CommonModule, ChartsModule],
  exports: [ModelStatisticsComponent],
})
export class ModelStatisticsModule {}
