import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ManageCollectionComponent } from './manage-collection.component';
import { MatCardModule } from '@angular/material/card';
import { CollectionManagerSubjectLeftModule } from '../../features/collection-manager-subject-left/collection-manager-subject-left.module';
import { CollectionManagerSubjectRightModule } from '../../features/collection-manager-subject-right/collection-manager-subject-right.module';
import { RouterModule } from '@angular/router';
import { BreadcrumbsContainerModule } from '../../features/breadcrumbs.container/breadcrumbs.container.module';
import {TranslateModule} from "@ngx-translate/core";

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
        TranslateModule,
      BreadcrumbsContainerModule,
    ],
})
export class ManageCollectionModule {}
