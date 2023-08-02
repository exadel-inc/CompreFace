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
import { Component, ElementRef, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { ServiceTypes } from 'src/app/data/enums/service-types.enum';

const BYTES_IN_MB = 1024 * 1024;

@Component({
  selector: 'app-drag-n-drop',
  templateUrl: './drag-n-drop.component.html',
  styleUrls: ['./drag-n-drop.component.scss'],
})
export class DragNDropComponent {
  @ViewChild('fileDropRef') fileDropEl: ElementRef;
  @Input() title: string;
  @Input() label: string;
  @Input()
  set maxImageSize(bytesValue: number) {
    if (bytesValue) {
      const mbValue = bytesValue / BYTES_IN_MB;
      this._maxImageSize = `${mbValue}Mb (${bytesValue} bytes)`;
    }
  }

  get maxImageSizeDisplay(): string {
    return this._maxImageSize;
  }

  displayDescription: boolean;

  private _maxImageSize: string;

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.router.url.includes('manage-collection') ? (this.displayDescription = false) : (this.displayDescription = true);
  }
  serviceType: ServiceTypes;

  viewComponentColumn: boolean;
  @Input('viewComponentColumn') set setViewComponentColumn(val: boolean | '') {
    this.viewComponentColumn = val === '' || val;
  }

  inline: boolean;
  @Input('inline') set setInline(val: boolean | '') {
    this.inline = val === '' || val;
  }

  @Output() upload: EventEmitter<File[]> = new EventEmitter();

  onChange(event): void {
    this.fileBrowseHandler(event.target.files);
    this.fileDropEl.nativeElement.value = null;
  }

  /**
   * on file drop handler
   */
  onFileDropped($event) {
    this.uploadFile($event);
  }

  /**
   * handle file from browsing
   */
  fileBrowseHandler(files) {
    this.uploadFile(files);
  }

  /**
   * Recognize face
   *
   * @param files (Files List)
   * TODO Send file to api
   */
  uploadFile(files: FileList) {
    if (files.length > 0) {
      this.upload.emit(Array.from(files));
    }
  }
}
