import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ApplicationListComponent } from './application-list-container.component';
import { ApplicationListFacade } from './application-list-facade.service';
import { TableModule } from '../table/table.module';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@NgModule({
  declarations: [ApplicationListComponent],
  exports: [ApplicationListComponent],
  providers: [ApplicationListFacade],
  imports: [CommonModule, TableModule, MatProgressSpinnerModule]
})
export class ApplicationListModule {}
