import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ApplicationListComponent } from './application-list-container.component';
import { ApplicationListFacade } from './application-list-facade';
import { TableModule } from '../table/table.module';
// import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SpinnerModule } from 'src/app/features/spinner/spinner.module';

@NgModule({
  declarations: [ApplicationListComponent],
  exports: [ApplicationListComponent],
  providers: [ApplicationListFacade],
  imports: [CommonModule, TableModule, SpinnerModule]
})
export class ApplicationListModule {}
