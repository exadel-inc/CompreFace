import { NgModule } from '@angular/core';
import { TruncatePipe } from './truncate.pipe';

@NgModule({
  declarations: [TruncatePipe],
  exports: [TruncatePipe],
})
export class TruncateModule {}
