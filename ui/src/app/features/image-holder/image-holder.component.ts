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
import { ChangeDetectorRef, EventEmitter, SimpleChanges } from '@angular/core';
import { Component, Input, Output, ChangeDetectionStrategy, OnChanges } from '@angular/core';

@Component({
	selector: 'image-holder',
	templateUrl: './image-holder.component.html',
	styleUrls: ['./image-holder.component.scss'],
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class ImageHolderComponent implements OnChanges {
	dataUrl: string;
	isDeleteVisible: boolean;
	isLoading: boolean;

	@Input() file: File;
	@Output() onDelete = new EventEmitter();

	constructor(private cd: ChangeDetectorRef) {}

	ngOnChanges(changes: SimpleChanges): void {
		if (changes?.file.currentValue) {
			this.readFile(this.file);
		}
	}

	readFile(file: File): void {
		const reader = new FileReader();

		reader.onload = (eve: any) => {
			this.dataUrl = eve.target.result;
			this.cd.detectChanges();
		};

		reader.readAsDataURL(file);
	}
}
