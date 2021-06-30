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
import { AfterViewInit, Component, ElementRef, EventEmitter, Input, Output, Renderer2, ViewChild } from '@angular/core';

@Component({
  selector: 'app-drag-n-drop',
  templateUrl: './drag-n-drop.component.html',
  styleUrls: ['./drag-n-drop.component.scss'],
})
export class DragNDropComponent implements AfterViewInit {
  @ViewChild('fileDropRef') fileDropEl: ElementRef;
  @Input() title: string;
  @Input() label: string;
  @Input() model: any;
  @Input('viewComponentColumn')
  get view() {
    return this.viewColumn;
  }
  set view(val: boolean) {
    this.viewColumn = true;
  }
  @Output() upload: EventEmitter<File> = new EventEmitter();

  viewColumn = false;

  constructor(private renderer: Renderer2, private elementRef: ElementRef<HTMLElement>) {}

  ngAfterViewInit(): void {
    const nativeElement: ChildNode = this.elementRef.nativeElement.firstChild.firstChild;
    const classValue = this.viewColumn ? 'column' : 'row';

    this.renderer.addClass(nativeElement, classValue);
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
  uploadFile(files: Array<any>) {
    if (files.length > 0) {
      this.upload.emit(files[0]);
    }
  }
}
