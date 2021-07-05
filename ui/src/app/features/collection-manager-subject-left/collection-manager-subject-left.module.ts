import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CollectionManagerSubjectLeftComponent } from './collection-manager-subject-left.component';
import { ApplicationListFacade } from '../application-list/application-list-facade';
import { MatListModule } from '@angular/material/list';
import { TranslateModule } from '@ngx-translate/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AppSearchTableModule } from '../app-search-table/app-search-table.module';

@NgModule({
  declarations: [CollectionManagerSubjectLeftComponent],
  imports: [CommonModule, MatListModule, TranslateModule, MatButtonModule, MatIconModule, AppSearchTableModule],
  exports: [CollectionManagerSubjectLeftComponent],
  providers: [ApplicationListFacade],
})
export class CollectionManagerSubjectLeftModule {}
