import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {ApplicationListComponent} from './application-list-container.component';
import {ApplicationListFacade} from './application-list-facade';
import {TableModule} from '../table/table.module';
import {SpinnerModule} from 'src/app/features/spinner/spinner.module';
import {MatButtonModule} from '@angular/material/button';
import { SnackBarModule } from 'src/app/features/snackbar/snackbar.module';

@NgModule({
  declarations: [ApplicationListComponent],
  exports: [ApplicationListComponent],
  providers: [ApplicationListFacade],
  imports: [CommonModule, TableModule, SpinnerModule, MatButtonModule, SnackBarModule]
})
export class ApplicationListModule {}
