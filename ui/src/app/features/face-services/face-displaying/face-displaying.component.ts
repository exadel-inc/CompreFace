import { AfterViewInit, Component, ElementRef, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, ViewChild } from '@angular/core';

import { LoadingPhotoService } from '../../../core/photo-loader/photo-loader.service';
import { ImageSize } from '../../../data/interfaces/image';
import { checkFile } from '../face-services.decorators';
import { filter, map, takeUntil, tap } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { recalculateFaceCoordinate } from '../face-services.helpers';

@Component({
  selector: 'app-face-displaying',
  template: '<canvas #canvasElement [width]="width"></canvas>',
})
export class FaceDisplayingComponent implements OnInit, OnChanges, AfterViewInit, OnDestroy {
  @Input() width: number;
  @Input() file: any;
  @Input() dataFrames: any[];

  @ViewChild('canvasElement', { static: false }) canvasElement: ElementRef;

  private unsubscribes$: Subject<void> = new Subject<void>();
  private ctx: CanvasRenderingContext2D;
  private sizeImage: ImageSize;
  private sizeCanvas: ImageSize;

  private frames: Subject<any> = new Subject();

  private recal;

  constructor(private loadingPhotoService: LoadingPhotoService) {}

  ngOnChanges(changes: SimpleChanges): void {
    this.loadImg(this.file);
    this.frames.next(this.dataFrames);
  }

  ngOnInit(): void {
    this.frames
      .pipe(
        takeUntil(this.unsubscribes$),
        filter(val => val),
        map(val => val.map(value => ({ ...value, box: recalculateFaceCoordinate(value.box, this.sizeImage, this.sizeCanvas) })))
      )
      .subscribe(val => {
        this.recal = val;
      });
  }

  ngAfterViewInit(): void {
    this.ctx = this.canvasElement.nativeElement.getContext('2d');
  }

  @checkFile
  loadImg(file: File): void {
    this.loadingPhotoService
      .loader(file)
      .pipe(
        takeUntil(this.unsubscribes$),
        tap(({ width, height }: ImageBitmap) => {
          this.sizeImage = { width, height };
          this.sizeCanvas = { width: this.width, height: (height / width) * this.width };
          this.canvasElement.nativeElement.setAttribute('height', this.sizeCanvas.height);
        })
      )
      .subscribe((img: ImageBitmap) => this.displayImg(img, this.sizeCanvas));
  }

  displayImg(img: ImageBitmap, size: ImageSize): void {
    this.ctx.drawImage(img, 0, 0, size.width, size.height);
  }

  ngOnDestroy(): void {
    this.unsubscribes$.next();
    this.unsubscribes$.complete();
  }
}
