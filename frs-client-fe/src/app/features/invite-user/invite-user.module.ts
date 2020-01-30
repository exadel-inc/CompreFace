import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {MatButtonModule} from "@angular/material/button";
import {InviteUserComponent} from "./invite-user.component";
import {ReactiveFormsModule} from "@angular/forms";
import {MatInputModule} from "@angular/material/input";
import {MatAutocompleteModule} from "@angular/material/autocomplete";


@NgModule({
  declarations: [InviteUserComponent],
  exports: [
    InviteUserComponent
  ],
  imports: [
    CommonModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatAutocompleteModule
  ],
})
export class InviteUserModule { }
