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
