import {Component, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from "rxjs";
import {Store} from "@ngrx/store";
import {AppState} from "../../../store/";

@Component({
  selector: 'app-alert',
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.sass']
})

export class AlertComponent implements OnInit, OnDestroy {
  public text: string;
  public type = 'success';
  aSub: Subscription;
  delay = 5000;

  constructor(private store: Store<AppState>) { }

  ngOnInit() {
    // this.aSub = this.alertService.alert$.subscribe( alert => {
    //   this.text = alert.text;
    //   this.type = alert.type;
    //
    //   const timeout = setTimeout(() => {
    //     clearTimeout(timeout);
    //     this.text = '';
    //   }, this.delay);
    // });
  }

  ngOnDestroy() {
    if (this.aSub) { this.aSub.unsubscribe(); }
  }
}
