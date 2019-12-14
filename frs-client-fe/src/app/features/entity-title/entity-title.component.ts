import {ChangeDetectionStrategy, Component, Input, OnInit, Output} from '@angular/core';
import {Organization} from "../../data/organization";

@Component({
  selector: 'app-entity-title',
  templateUrl: './entity-title.component.html',
  styleUrls: ['./entity-title.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EntityTitleComponent implements OnInit {
  isEditing = false;
  optionName: string;
  @Input() options: [Organization];
  @Output() selected: string;
  @Output() name: string;

  constructor() { }

  ngOnInit() {
  }

  discard() {
    this.isEditing = false;
  }

  apply() {
    this.name = this.optionName;
    this.isEditing = false;
  }

}
