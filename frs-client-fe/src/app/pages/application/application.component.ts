import { Component, OnDestroy, OnInit } from '@angular/core';
import { ApplicationPageService } from "./application.service";

@Component({
  selector: 'app-application',
  templateUrl: './application.component.html',
  styleUrls: ['./application.component.sass']
})
export class ApplicationComponent implements OnInit, OnDestroy {
  constructor(private appService: ApplicationPageService) { }

  ngOnInit() {
    this.appService.initUrlBindingStreams();
  }

  ngOnDestroy() {
    this.appService.unSubscribe();
  }
}
