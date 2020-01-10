import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserListComponent } from './user-list.component';
import { UserListFacade } from './user-list-facade';
import { UserTableModule } from '../user-table/user-table.module';
// import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SpinnerModule } from 'src/app/features/spinner/spinner.module';

@NgModule({
  declarations: [UserListComponent],
  exports: [UserListComponent],
  providers: [UserListFacade],
  imports: [CommonModule, UserTableModule, SpinnerModule]
})
export class UserListModule {}
