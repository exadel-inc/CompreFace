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

import { Component, ViewChild, ElementRef, Input } from '@angular/core';
import { Model } from 'src/app/data/model';
import { DragNDropService } from './drag-n-drop.service';

@Component({
  selector: 'app-drag-n-drop',
  templateUrl: './drag-n-drop.component.html',
  styleUrls: ['./drag-n-drop.component.scss']
})
export class DragNDropComponent {

  @ViewChild('fileDropRef', { static: false }) fileDropEl: ElementRef;
  file: any;
  data: object;
  loading = true;
  @Input() model: Model;

  constructor(private dragService: DragNDropService) {}

  /**
   * on file drop handler
   */
  onFileDroppedAdd($event) {
    this.processFileAddToModel($event);
  }

  /**
   * on file drop handler
   */
  onFileDroppedReco($event) {
    this.processFileRecoFace($event);
  }

  /**
   * handle file from browsing
   */
  fileBrowseHandlerAdd(files) {
    this.processFileAddToModel(files);
  }

  /**
   * handle file from browsing
   */
  fileBrowseHandlerReco(files) {
    this.processFileRecoFace(files);
  }

  /**
   * Add faces to model
   * @param files (Files List)
   * TODO Send file to api
   */
  processFileAddToModel(files: Array<any>) {
    for (const item of files) {
      this.file = item;
    }
    this.dragService.addFace(this.file, this.model).subscribe((res) => {
      this.data = res;
      this.loading = false;
    });
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
    console.log(files.length);
    this.dragService.recognize(this.file, this.model).subscribe((res) => {
      this.data = res;
      this.loading = false;
    });
  }

  /**
   * Train model
   */
  trainModel() {
    this.dragService.train(this.model).subscribe((res) => {
      this.data = res;
      this.loading = false;
    });
  }

}
