import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ChartsModule } from 'ng2-charts';
import { ModelStatisticsComponent } from './model-statistics.component';

@NgModule({
  declarations: [ModelStatisticsComponent],
  imports: [CommonModule, TranslateModule, ChartsModule],
  exports: [ModelStatisticsComponent],
})
export class ModelStatisticsModule {}
