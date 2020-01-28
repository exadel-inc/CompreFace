import { Component, OnDestroy, OnInit } from '@angular/core';
import {ModelPageService} from "./model.service";

@Component({
  selector: 'app-model',
  templateUrl: './model.component.html',
  styleUrls: ['./model.component.sass']
})
export class ModelComponent implements OnInit, OnDestroy {
  constructor(private modelService: ModelPageService) { }

  ngOnInit() {
    this.modelService.initUrlBindingStreams();
  }

  ngOnDestroy() {
    this.modelService.unSubscribe();
  }
}
