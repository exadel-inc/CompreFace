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

@Component({
  selector: 'app-drag-n-drop',
  templateUrl: './drag-n-drop.component.html',
  styleUrls: ['./drag-n-drop.component.scss'],
})
export class DragNDropComponent {
  @ViewChild('fileDropRef') fileDropEl: ElementRef;
  @Input() title: string;
  @Input() label: string;

  viewComponentColumn: boolean;
  @Input('viewComponentColumn') set setViewComponentColumn(val: boolean | '') {
    this.viewComponentColumn = val === '' || val;
  }

  inline: boolean;
  @Input('inline') set setInline(val: boolean | '') {
    this.inline = val === '' || val;
  }

  @Output() upload: EventEmitter<File[]> = new EventEmitter();

  constructor() {}

  onChange(event): void {
    this.fileBrowseHandler(event.target.files);
    this.fileDropEl.nativeElement.value = null;
  }

  /**
   * on file drop handler
   */
  onFileDropped($event): void {
    this.uploadFile($event);
  }

  /**
   * handle file from browsing
   */
  fileBrowseHandler(files): void {
    this.uploadFile(files);
  }

  /**
   * Recognize face
   *
   * @param files (Files List)
   * TODO Send file to api
   */
  uploadFile(files: FileList): void {
    if (files.length > 0) {
      this.upload.emit(Array.from(files));
    }
  }
}
