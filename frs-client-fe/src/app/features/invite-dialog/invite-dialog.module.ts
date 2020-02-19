import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {InviteDialogComponent} from './invite-dialog.component';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule, MatInputModule, MatButtonModule, MatAutocompleteModule} from '@angular/material';
import {CommonModule} from '@angular/common';
import {MatDialogModule} from '@angular/material/dialog';

@NgModule({
  declarations: [InviteDialogComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatDialogModule,
    MatButtonModule,
    MatAutocompleteModule
  ],
  exports: [InviteDialogComponent]
})
export class InviteDialogModule {}
