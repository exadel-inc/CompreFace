import { Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Model } from '../../../data/interfaces/model';
import { Observable, Subscription } from 'rxjs';
import { map, tap } from 'rxjs/operators';

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
  @Input() set printData(value: any) {
    if (this.printSubscription) {
      this.printSubscription.unsubscribe();
    }

    if (value) {
      this.printSubscription = this.printResult(value.box, value.faces).subscribe();
    }
  }

  private printSubscription: Subscription;
  private canvasSize: ImageSize = {width: 250, height: 250};

  @ViewChild('canvasElement', { static: true }) myCanvas: ElementRef;

  constructor() {
  }

  ngOnDestroy() {
    if (this.printSubscription) {
      this.printSubscription.unsubscribe();
    }
  }

  /*
   * Print result.
   *
   * @param box Box
   * @param face Face
   */
  printResult(box: any, face: any): Observable<any> {
    return getImageSize(this.file).pipe(
      map((imageSize) => recalculateFaceCoordinate(box, imageSize, this.canvasSize)),
      tap((recalculatedBox) => this.drawCanvas(recalculatedBox, face))
    );
  }

  /*
   * Print the result of recognition.
   *
   * @param box Box
   * @param face Face
   */
  drawCanvas(box: any, face: any) {
    const img = new Image();
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
      ctx.fillText(box.probability, box.x_min + 10, box.y_max + 20);
      ctx.fillText(face[ 0 ].face_name, box.x_min + 10, box.y_min - 5);
    };
    img.src = URL.createObjectURL(this.file);
  }
}

interface ImageSize {
  width: any;
  height: any;
}

/**
 * Get image size.
 *
 * @param file File.
 */
function getImageSize(file: File): Observable<ImageSize> {
  let img = new Image();

  return new Observable((subscriber) => {
    img = new Image();
    img.onload = () => {
        subscriber.next({width: img.width, height: img.height});
        subscriber.complete();
      };
    img.src = URL.createObjectURL(file);
  });
}

function recalculateFaceCoordinate(box: any, imageSize: ImageSize, sizeToCalc: ImageSize): any {
  const divideWidth = imageSize.width / sizeToCalc.width;
  const divideHeight = imageSize.height / sizeToCalc.height;

  return {
    ...box,
    x_max: box.x_max / divideWidth,
    x_min: box.x_min / divideWidth,
    y_max: box.y_max / divideHeight,
    y_min: box.y_min / divideHeight
  };
}
