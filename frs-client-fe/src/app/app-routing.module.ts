import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {MainLayoutComponent} from "./ui/main-layout/main-layout.component";


const routes: Routes = [
  { path: '', component: MainLayoutComponent, children: [
      { path: 'organization', loadChildren: './pages/organization/organization.module#OrganizationModule'}
    ]},
  { path: 'admin', loadChildren: './pages/admin/admin.module#AdminModule'}
  ];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
