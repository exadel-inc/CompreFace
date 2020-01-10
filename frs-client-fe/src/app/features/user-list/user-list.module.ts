import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserListComponent } from './user-list.component';
import { UserListFacade } from './user-list-facade.service';
import { UserTableModule } from '../user-table/user-table.module';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@NgModule({
  declarations: [UserListComponent],
  exports: [UserListComponent],
  providers: [UserListFacade],
  imports: [CommonModule, UserTableModule, MatProgressSpinnerModule]
})
export class UserListModule {}
