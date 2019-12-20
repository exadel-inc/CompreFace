import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Organization} from "../../data/organization";

@Component({
  selector: 'app-entity-title',
  templateUrl: './entity-title.component.html',
  styleUrls: ['./entity-title.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EntityTitleComponent implements OnInit {
  editing = false;
  newName: string;
  selectedId: string;
  @Input() options: [Organization];
  @Input() renameDisable: boolean;
  @Input() selectId: string;
  @Input() entityName: string;
  @Output() selectIdChange = new EventEmitter();
  @Output() rename = new EventEmitter();
  constructor() { }

  ngOnInit() {
    this.selectedId = this.selectId;
  }

  set selected(value) {
    console.log(value);
    this.selectedId = (value);
    this.selectIdChange.emit(value);
  }

  get selected() {
    return this.selectedId;
  }

  discard() {
    this.editing = false;
  }

  apply() {
    console.log(this.newName);
    this.rename.emit(this.newName);

    this.editing = false;
  }

  edit() {
    this.editing = true;
    this.newName = this.options.find(option => option.id === this.selectId).name;
  }
}
