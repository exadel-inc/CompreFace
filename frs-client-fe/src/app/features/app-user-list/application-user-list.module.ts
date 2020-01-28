import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ApplicationUserListComponent } from './application-user-list.component';
import { ApplicationUserListFacade } from './application-user-list-facade';
import { UserTableModule } from '../user-table/user-table.module';
import { SpinnerModule } from 'src/app/features/spinner/spinner.module';
import { MatFormFieldModule, MatInputModule, MatButtonModule } from '@angular/material';
import {InviteUserModule} from "../invite-user/invite-user.module";

@NgModule({
  declarations: [ApplicationUserListComponent],
  exports: [ApplicationUserListComponent],
  providers: [ApplicationUserListFacade],
  imports: [
    CommonModule,
    UserTableModule,
    SpinnerModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    ReactiveFormsModule,
    InviteUserModule
  ]
})
export class AppUserListModule {}
