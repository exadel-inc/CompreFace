import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UserListComponent} from './user-list.component';
import {UserListFacade} from './user-list-facade';
import {UserTableModule} from '../user-table/user-table.module';
import {SpinnerModule} from 'src/app/features/spinner/spinner.module';
import {InviteUserModule} from '../invite-user/invite-user.module';
import {FormsModule} from '@angular/forms';
import {MatInputModule} from '@angular/material/input';
import {UserTablePipeModule} from '../../ui/search-pipe/user-table-filter.module';
import {MatCardModule} from '@angular/material/card';
import {InviteDialogModule} from '../invite-dialog/invite-dialog.module';
import {MatDialogModule, MatButtonModule} from '@angular/material';
import {InviteDialogComponent} from '../invite-dialog/invite-dialog.component';

@NgModule({
  declarations: [UserListComponent],
  exports: [UserListComponent],
  providers: [UserListFacade, ],
  imports: [
    CommonModule,
    UserTableModule,
    SpinnerModule,
    InviteUserModule,
    FormsModule,
    UserTablePipeModule,
    MatInputModule,
    MatCardModule,
    MatDialogModule,
    InviteDialogModule,
    MatButtonModule
  ],
  entryComponents: [InviteDialogComponent]
})
export class UserListModule {}
