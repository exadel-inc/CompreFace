import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ManageCollectionComponent } from './manage-collection.component';
import { CollectionManagerSubjectLeftComponent } from '../../features/collection-manager-subject-left/collection-manager-subject-left.component';
import { CollectionManagerSubjectRightComponent } from '../../features/collection-manager-subject-right/collection-manager-subject-right.component';
import { MatCardModule } from '@angular/material/card';

@NgModule({
  declarations: [ManageCollectionComponent, CollectionManagerSubjectLeftComponent, CollectionManagerSubjectRightComponent],
  imports: [CommonModule, MatCardModule],
})
export class ManageCollectionModule {}
