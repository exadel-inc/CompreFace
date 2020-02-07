import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {RouterModule} from '@angular/router';
import {AuthGuard} from '../../core/auth/auth.guard';
import {ToolBarModule} from '../../features/tool-bar/tool-bar.module';
import {MatCardModule} from '@angular/material/card';
import {BreadcrumbsModule} from '../../features/breadcrumbs/breadcrumbs.module';
import {ModelHeaderModule} from '../../features/model-header/model-header.module';
import {ModelPageService} from './model.service';
import {ModelComponent} from './model.component';
import {ModelRelationListModule} from 'src/app/features/models-relation-list/models-relation-list.model';

@NgModule({
  declarations: [ModelComponent],
  imports: [
    CommonModule,
    MatButtonModule,
    RouterModule.forChild([
      { path: '', data: {routeName: 'model'}, component: ModelComponent, canActivate: [AuthGuard] },
    ]),
    ToolBarModule,
    BreadcrumbsModule,
    ModelHeaderModule,
    MatCardModule,
    ModelRelationListModule
  ],
  providers: [ModelPageService]
})
export class ModelModule { }
