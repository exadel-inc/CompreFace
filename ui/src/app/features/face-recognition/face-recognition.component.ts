import { Component, OnDestroy, OnInit } from '@angular/core';
import { combineLatest, Observable } from 'rxjs';
import { Model } from '../../data/interfaces/model';
import { Store } from '@ngrx/store';
import { AppState } from '../../store';
import { recognizeFace, recognizeFaceReset } from '../../store/face-recognition/actions';
import { selectTest, selectFile, selectRequest,
  selectTestIsPending } from '../../store/face-recognition/selectors';
import { selectCurrentModel } from '../../store/model/selectors';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-face-recognition',
  templateUrl: './face-recognition.component.html',
  styleUrls: ['./face-recognition.component.scss']
})
export class FaceRecognitionComponent implements OnInit, OnDestroy {
  public model$: Observable<Model>;
  public data$: Observable<any>;
  public file$: Observable<any>;
  public requestInfo$: Observable<any>;
  public pending$: Observable<boolean>;
  public isDisplayResult$: Observable<boolean>;

  constructor(private store: Store<AppState>) {
    // Component constructor.
  }

  ngOnInit() {
    this.model$ = this.store.select(selectCurrentModel);
    this.data$ = this.store.select(selectTest);
    this.file$ = this.store.select(selectFile);
    this.requestInfo$ = this.store.select(selectRequest);
    this.pending$ = this.store.select(selectTestIsPending);
    this.isDisplayResult$ = combineLatest([this.data$, this.pending$]).pipe(
      map(([data, pending]) => !!data && !pending)
    );
  }

  ngOnDestroy() {
    this.store.dispatch(recognizeFaceReset());
  }

  recognizeFace({file, model}: {file: File, model: Model}) {
    return this.store.dispatch(recognizeFace({
      file,
      model
    }));
  }
}
