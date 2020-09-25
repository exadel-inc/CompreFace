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

import { Component, ViewChild, ElementRef, Input, Output } from '@angular/core';
import { Model } from 'src/app/data/model';
import { DragNDropService } from './drag-n-drop.service';
import { EventEmitter } from 'protractor';

@Component({
  selector: 'app-drag-n-drop',
  templateUrl: './drag-n-drop.component.html',
  styleUrls: ['./drag-n-drop.component.scss']
})
export class DragNDropComponent {

  @ViewChild('fileDropRef', { static: false }) fileDropEl: ElementRef;
  file: any;
  @Input() data: object;
  @Input() loading = true;
  @Input() model: Model;
  @Output() recognizeFace = new EventEmitter();

  constructor(private dragService: DragNDropService) {}

  /**
   * on file drop handler
   */
  onFileDroppedReco($event) {
    this.processFileRecoFace($event);
  }

  /**
   * handle file from browsing
   */
  fileBrowseHandlerReco(files) {
    this.processFileRecoFace(files);
  }

  /**
   * Recognize face
   * @param files (Files List)
   * TODO Send file to api
   */
  processFileRecoFace(files: Array<any>) {
    for (const item of files) {
      this.file = item;
    }
    this.recognizeFace.emit(this.file, this.model);
  }
}
