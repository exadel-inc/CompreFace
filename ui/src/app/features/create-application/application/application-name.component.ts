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
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatStepper } from '@angular/material/stepper';
import { Subscription } from 'rxjs';
import { MAX_INPUT_LENGTH } from 'src/app/core/constants';

enum DefaultAppName {
  DemoApp = 'Demo app',
}

@Component({
  selector: 'application-name',
  templateUrl: './application-name.component.html',
  styleUrls: ['../shared-styles.component.scss'],
})
export class ApplicationNameComponent implements OnInit, OnDestroy {
  @Output() applicationName = new EventEmitter<{ applicationName: string }>();
  @Input() stepper: MatStepper;
  application: FormGroup;
  valueSubs: Subscription;
  isValid: boolean = true;
  maxInputLength: number = MAX_INPUT_LENGTH;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.application = this.fb.group({
      applicationName: ['', Validators.required],
    });

    this.valueSubs = this.application.get('applicationName').valueChanges.subscribe(val => {
      if (val === DefaultAppName.DemoApp) {
        this.isValid = false;
        return;
      }

      if (val === this.maxInputLength) {
        this.isValid = false;
        return;
      }

      this.isValid = true;
    });
  }

  onNext(): void {
    if (!this.isValid) return;
    this.stepper.next();
    this.applicationName.emit(this.application.value.applicationName);
  }

  ngOnDestroy(): void {
    this.valueSubs.unsubscribe();
  }
}
