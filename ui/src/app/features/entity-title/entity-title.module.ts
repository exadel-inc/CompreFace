import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {EntityTitleComponent} from './entity-title.component';
import {MatButtonModule} from '@angular/material/button';
import {MatInputModule} from '@angular/material/input';
import {MatIconModule} from '@angular/material/icon';
import {MatSelectModule} from '@angular/material/select';
import {FormsModule} from '@angular/forms';



@NgModule({
  declarations: [EntityTitleComponent],
  exports: [
    EntityTitleComponent
  ],
  imports: [
    CommonModule,
    MatButtonModule,
    MatInputModule,
    MatIconModule,
    MatSelectModule,
    FormsModule,
  ]
})
export class EntityTitleModule { }
