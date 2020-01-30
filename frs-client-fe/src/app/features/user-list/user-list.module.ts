import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserListComponent } from './user-list.component';
import { UserListFacade } from './user-list-facade';
import { UserTableModule } from '../user-table/user-table.module';
import { SpinnerModule } from 'src/app/features/spinner/spinner.module';
import {InviteUserModule} from "../invite-user/invite-user.module";

@NgModule({
  declarations: [UserListComponent],
  exports: [UserListComponent],
  providers: [UserListFacade],
  imports: [
    CommonModule,
    UserTableModule,
    SpinnerModule,
    InviteUserModule
  ]
})
export class UserListModule {}
