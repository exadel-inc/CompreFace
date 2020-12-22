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
    path: '',
    component: MainLayoutComponent,
    children: [{ path: '', loadChildren: () => import('./pages/dashboard/dashboard.module').then(m => m.DashboardModule) }],
  },
  {
    path: 'application',
    component: MainLayoutComponent,
    children: [{ path: '', loadChildren: () => import('./pages/application/application.module').then(m => m.ApplicationModule) }],
  },
  {
    path: 'test-model',
    component: MainLayoutComponent,
    children: [{ path: '', loadChildren: () => import('./pages/test-model/test-model.module').then(m => m.TestModelModule) }],
  },
  {
    path: 'demo',
    component: MainLayoutComponent,
    children: [{ path: '', loadChildren: () => import('./pages/demo/demo.module').then(m => m.DemoModule) }],
  },
  { path: 'login', loadChildren: () => import('./pages/login/login.module').then(m => m.LoginModule) },
  { path: 'sign-up', loadChildren: () => import('./pages/sign-up/sign-up.module').then(m => m.SignUpModule) },
  { path: '**', redirectTo: '/' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
