import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ManageCollectionComponent } from './manage-collection.component';
import { MatCardModule } from '@angular/material/card';
import { CollectionManagerSubjectLeftModule } from '../../features/collection-manager-subject-left/collection-manager-subject-left.module';
import { CollectionManagerSubjectRightModule } from '../../features/collection-manager-subject-right/collection-manager-subject-right.module';
import { RouterModule } from '@angular/router';

@NgModule({
  declarations: [ManageCollectionComponent],
  imports: [
    CommonModule,
    MatCardModule,
    CollectionManagerSubjectLeftModule,
    CollectionManagerSubjectRightModule,
    RouterModule.forChild([
      {
        path: '',
        component: ManageCollectionComponent,
      },
    ]),
  ],
})
export class ManageCollectionModule {}
