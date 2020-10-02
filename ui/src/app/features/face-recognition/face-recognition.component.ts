import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { Model } from '../../data/model';
import { Store } from '@ngrx/store';
import { AppState } from '../../store';
import { recognizeFace, addFace } from '../../store/face-recognition/actions';

@Component({
  selector: 'app-face-recognition',
  templateUrl: './face-recognition.component.html',
  styleUrls: ['./face-recognition.component.scss']
})
export class FaceRecognitionComponent implements OnInit {
  public model$: Observable<Model>;
  public data$: Observable<string>;

  constructor(private store: Store<AppState>) {
    // Component constructor.
  }

  ngOnInit() {
    // this.model$ = this.store.select();
    // this.data$ = this.store.select();
  }

  recognizeFace({file, model}: {file: File, model: Model}) {
    return this.store.dispatch(recognizeFace({
      file,
      model
    }));
  }

  addFace({file, model}: {file: File, model: Model}) {
    return this.store.dispatch(addFace({
      file,
      model
    }));
  }
}
