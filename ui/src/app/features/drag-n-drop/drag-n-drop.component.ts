import { Component, ElementRef, EventEmitter,
  Input, OnInit, Output, ViewChild } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Model } from '../../data/interfaces/model';

@Component({
  selector: 'app-drag-n-drop',
  templateUrl: './drag-n-drop.component.html',
  styleUrls: ['./drag-n-drop.component.scss']
})
export class DragNDropComponent implements OnInit {
  @ViewChild('fileDropRef') fileDropEl: ElementRef;
  @Input() title: string;
  @Input() label: string;
  @Input() model: any;
  @Output() upload: EventEmitter<{file: File, model: Model}> = new EventEmitter();
  private file: any;

  constructor(private translate: TranslateService) {
    // Set the default title and label. But leave possibility to set another title and label.
    this.title = this.translate.instant('dnd.title');
    this.label = this.translate.instant('dnd.label');
  }

  ngOnInit() {
    // do something.
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
   * @param files (Files List)
   * TODO Send file to api
   */
  uploadFile(files: Array<any>) {
    this.file = files[0];
    this.upload.emit({file: this.file, model: this.model});
  }
}
