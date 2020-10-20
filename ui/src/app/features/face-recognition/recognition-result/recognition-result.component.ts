import { Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Model } from '../../../data/interfaces/model';
import { Observable, Subscription } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { getImageSize, ImageSize, recalculateFaceCoordinate } from '../face-recognition.helpers';

@Component({
  selector: 'app-recognition-result',
  templateUrl: './recognition-result.component.html',
  styleUrls: ['./recognition-result.component.scss']
})
export class RecognitionResultComponent implements OnDestroy {
  @Input() pending = true;
  @Input() model: Model;
  @Input() file: File;
  @Input() requestInfo: any;
  // Handle input changes and update image.
  @Input() set printData(value: any) {
    if (this.printSubscription) {
      this.printSubscription.unsubscribe();
    }

    if (value) {
      this.printSubscription = this.printResult(value.box, value.faces).subscribe();
    }
  }

  private printSubscription: Subscription;
  public canvasSize: ImageSize = {width: 300, height: null};

  @ViewChild('canvasElement', { static: true }) myCanvas: ElementRef;

  constructor() {
  }

  ngOnDestroy() {
    if (this.printSubscription) {
      this.printSubscription.unsubscribe();
    }
  }

  /*
   * Print result on template.
   *
   * @param box Box
   * @param face Face
   */
  printResult(box: any, face: any): Observable<any> {
    return getImageSize(this.file).pipe(
      tap(({ width, height }) => {
        this.canvasSize.height = (height / width) * this.canvasSize.width;
        this.myCanvas.nativeElement.setAttribute('height', this.canvasSize.height);
      }),
      map((imageSize) => recalculateFaceCoordinate(box, imageSize, this.canvasSize)),
      tap((recalculatedBox) => this.drawCanvas(recalculatedBox, face))
    );
  }

  /*
   * Make canvas and draw face and info on image.
   *
   * @param box Face coordinates from BE.
   * @param face.
   */
  drawCanvas(box: any, face: any) {
    const img = new Image();
    const resultFace = face.length > 0 ? face[0] : { face_name: undefined, similarity: 0 };
    const ctx: CanvasRenderingContext2D =
      this.myCanvas.nativeElement.getContext('2d');

    img.onload = () => {
      ctx.drawImage(img, 0, 0, this.canvasSize.width, this.canvasSize.height);
      ctx.beginPath();
      ctx.strokeStyle = 'green';
      ctx.moveTo(box.x_min, box.y_min);
      ctx.lineTo(box.x_max, box.y_min);
      ctx.lineTo(box.x_max, box.y_max);
      ctx.lineTo(box.x_min, box.y_max);
      ctx.lineTo(box.x_min, box.y_min);
      ctx.stroke();
      ctx.fillStyle = 'green';
      ctx.fillRect(box.x_min, box.y_min - 25, box.x_max - box.x_min, 25);
      ctx.fillRect(box.x_min, box.y_max, box.x_max - box.x_min, 25);
      ctx.fillStyle = 'white';
      ctx.font = '12pt Roboto Regular Helvetica Neue sans-serif';
      ctx.fillText(resultFace.similarity, box.x_min + 10, box.y_max + 20);
      ctx.fillText(resultFace.face_name, box.x_min + 10, box.y_min - 5);
    };
    img.src = URL.createObjectURL(this.file);
  }
}
