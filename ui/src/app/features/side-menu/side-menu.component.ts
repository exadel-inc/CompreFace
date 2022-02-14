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

import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Routes } from 'src/app/data/enums/routers-url.enum';

@Component({
  selector: 'side-menu',
  templateUrl: './side-menu.component.html',
  styleUrls: ['./side-menu.component.scss'],
})
export class SideMenuComponent {
  app: string;
  model: string;
  type: string;
  closed: boolean = true;

  constructor(private router: Router, private route: ActivatedRoute) {}

  ngOnInit() {
    this.app = this.route.snapshot.queryParams.app;
    this.model = this.route.snapshot.queryParams.model;
    this.type = this.route.snapshot.queryParams.type;
  }

  onDashBoard(): void {
    this.router.navigate([Routes.Dashboard]);
  }

  onTest(): void {
    this.router.navigate([Routes.TestModel], {
      queryParams: {
        app: this.app,
        model: this.model,
        type: this.type,
      },
    });
  }

  onManageCollection(): void {
    this.router.navigate([Routes.ManageCollection], {
      queryParams: {
        app: this.app,
        model: this.model,
      },
    });
  }
}
