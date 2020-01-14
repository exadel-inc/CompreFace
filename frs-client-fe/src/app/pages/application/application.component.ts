import {Component, OnDestroy, OnInit} from '@angular/core';
import {ApplicationService} from "./application.service";
import {Observable} from "rxjs";

@Component({
  selector: 'app-application',
  templateUrl: './application.component.html',
  styleUrls: ['./application.component.sass']
})
export class ApplicationComponent implements OnInit, OnDestroy {
  app$: any;
  isLoading$: Observable<boolean>;

  constructor(private appService: ApplicationService) {}

  ngOnInit() {
    this.appService.initUrlBindingStreams();
    this.isLoading$ = this.appService.isLoading$;
    this.app$ = this.appService.app$;
  }

  ngOnDestroy() {
    this.appService.unSubscribe();
  }

}
