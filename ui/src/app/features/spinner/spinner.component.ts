import {Component, OnInit, Input} from '@angular/core';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-spinner',
  templateUrl: './spinner.component.html',
  styleUrls: ['./spinner.component.scss']
})
export class SpinnerComponent implements OnInit {
  @Input() isVisible$: Observable<boolean>;
  constructor() { }

  ngOnInit() {
  }
}
