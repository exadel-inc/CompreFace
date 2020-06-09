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

import {ChangeDetectionStrategy, Component, OnInit, Output, EventEmitter, Input} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {EMAIL_REGEXP_PATTERN} from 'src/app/core/constants';
import {combineLatest, Observable} from 'rxjs';
import {map, startWith} from 'rxjs/operators';

@Component({
  selector: 'app-invite-user',
  templateUrl: './invite-user.component.html',
  styleUrls: ['./invite-user.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InviteUserComponent implements OnInit {
  public form: FormGroup;
  @Input() options$: Observable<string[]>;
  filteredOptions$: Observable<string[]>;
  @Output() email = new EventEmitter<string>();

  constructor() { }

  ngOnInit() {
    this.form = new FormGroup({
      email: new FormControl(null, [Validators.pattern(EMAIL_REGEXP_PATTERN)])
    });

    if (this.options$) {
      this.filteredOptions$ = combineLatest(
        this.options$,
        this.form.controls.email.valueChanges.pipe(startWith(''))
      ).pipe(
        map(([options, value]) => this.filter(options, value)),
      );
    }
  }

  public onClick(): void {
    if (this.form.valid && this.form.value.email && this.form.value.email.length) {
      this.email.emit(this.form.value.email);
      this.form.reset();
    }
  }

  private filter(options: string[], value: string): string[] {
    const filterValue = value ? value.toLowerCase() : '';
    return options ? options.filter(option => option.toLowerCase().indexOf(filterValue) === 0) : [''];
  }
}
