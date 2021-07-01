import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CollectionManagerSubjectLeftComponent } from './collection-manager-subject-left.component';
import { ApplicationListFacade } from '../application-list/application-list-facade';

@NgModule({
  declarations: [CollectionManagerSubjectLeftComponent],
  imports: [CommonModule],
  exports: [CollectionManagerSubjectLeftComponent],
  providers: [ApplicationListFacade],
})
export class CollectionManagerSubjectLeftModule {}
