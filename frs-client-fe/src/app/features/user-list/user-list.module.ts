import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { UserListComponent } from './user-list.component';
import { UserListFacade } from './user-list-facade';
import { UserTableModule } from '../user-table/user-table.module';
import { SpinnerModule } from 'src/app/features/spinner/spinner.module';
import {InviteUserComponent} from '../invite-user/invite-user.component';
import { MatFormFieldModule, MatInputModule, MatButtonModule } from '@angular/material';

@NgModule({
  declarations: [UserListComponent, InviteUserComponent],
  exports: [UserListComponent, InviteUserComponent],
  providers: [UserListFacade],
  imports: [CommonModule, UserTableModule, SpinnerModule, FormsModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, ReactiveFormsModule]
})
export class UserListModule {}
