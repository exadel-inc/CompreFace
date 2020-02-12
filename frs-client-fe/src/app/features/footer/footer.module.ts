import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FooterComponent} from './footer.component';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';

@NgModule({
  declarations: [FooterComponent],
  exports: [
    FooterComponent,
  ],
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
  ]
})
export class FooterModule { }
