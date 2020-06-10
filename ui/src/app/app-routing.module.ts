/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { MainLayoutComponent } from './ui/main-layout/main-layout.component';

const routes: Routes = [
  {
    path: '', component: MainLayoutComponent,
    children: [
      { path: '', loadChildren: './pages/organization/organization.module#OrganizationModule' }
    ]
  },
  {
    path: 'application', component: MainLayoutComponent, children: [
      { path: '', loadChildren: './pages/application/application.module#ApplicationModule' }
    ]
  },
  { path: 'login', loadChildren: './pages/login/login.module#LoginModule' },
  { path: 'sign-up', loadChildren: './pages/sign-up/sign-up.module#SignUpModule' },
  { path: '**', redirectTo: '/' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})

export class AppRoutingModule {
}
