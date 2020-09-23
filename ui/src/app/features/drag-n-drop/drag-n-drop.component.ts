import { Component, ViewChild, ElementRef } from '@angular/core';

@Component({
  selector: 'app-drag-n-drop',
  templateUrl: './drag-n-drop.component.html',
  styleUrls: ['./drag-n-drop.component.scss']
})
export class DragNDropComponent {

  @ViewChild('fileDropRef', { static: false }) fileDropEl: ElementRef;
  file: any;

  /**
   * on file drop handler
   */
  onFileDropped($event) {
    this.processFile($event);
  }

  /**
   * handle file from browsing
   */
  fileBrowseHandler(files) {
    this.processFile(files);
  }

  /**
   * Process file
   * @param files (Files List)
   * TODO Send file to api
   */
  processFile(files: Array<any>) {
    this.file = files[0];
  }
}
