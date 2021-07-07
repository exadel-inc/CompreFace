import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CollectionManagerSubjectLeftComponent } from './collection-manager-subject-left.component';
import { MatListModule } from '@angular/material/list';
import { TranslateModule } from '@ngx-translate/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AppSearchTableModule } from '../app-search-table/app-search-table.module';
import {CollectionLeftFacade} from "./collection-left-facade";
import {SpinnerModule} from "../spinner/spinner.module";

@NgModule({
  declarations: [CollectionManagerSubjectLeftComponent],
    imports: [CommonModule, MatListModule, TranslateModule, MatButtonModule, MatIconModule, AppSearchTableModule, SpinnerModule],
  exports: [CollectionManagerSubjectLeftComponent],
  providers: [CollectionLeftFacade],
})
export class CollectionManagerSubjectLeftModule {}
