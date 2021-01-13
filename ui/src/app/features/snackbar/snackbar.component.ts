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
import { Component, Inject } from '@angular/core';
import { MAT_SNACK_BAR_DATA } from '@angular/material/snack-bar';

@Component({
  selector: 'app-snackbar',
  styleUrls: ['./snackbar.component.scss'],
  templateUrl: './snackbar.component.html',
})
export class AppSnackBarComponent {
  public type: string;
  public message: string;

  constructor(@Inject(MAT_SNACK_BAR_DATA) data: { type: string; message: string }) {
    this.type = data.type;
    this.message = data.message;
  }
}
