import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from "@angular/router";
import {AuthGuard} from "../../core/auth/auth.guard";
import {TestModelComponent} from "./test-model.component";
import {BreadcrumbsModule} from "../../features/breadcrumbs/breadcrumbs.module";
import {MatCardModule} from "@angular/material/card";
import {TestModelPageService} from "./test-model.service";
import {ModelTestComponent} from "../../features/model-test/model-test.component";


@NgModule({
  declarations: [TestModelComponent, ModelTestComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      {path: '', component: TestModelComponent, canActivate: [AuthGuard]},
    ]),
    BreadcrumbsModule,
    MatCardModule,
  ],
  providers: [TestModelPageService]
})
export class TestModelModule {
}
