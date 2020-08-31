import {Component, OnDestroy, OnInit} from '@angular/core';
import {ModelService} from "../../core/model/model.service";
import {TestModelPageService} from "./test-model.service";

@Component({
  selector: 'app-test-model',
  templateUrl: './test-model.component.html',
  styleUrls: ['./test-model.component.scss']
})
export class TestModelComponent implements OnInit, OnDestroy {
  constructor(private modelService: TestModelPageService) {}

  ngOnInit() {
    this.modelService.initUrlBindingStreams();
  }
  ngOnDestroy(): void {
    this.modelService.clearSelectedModelId();
    this.modelService.unSubscribe();
  }
}
