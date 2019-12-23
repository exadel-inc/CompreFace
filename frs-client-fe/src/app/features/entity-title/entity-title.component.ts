import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Organization} from "../../data/organization";
import {Observable} from "rxjs";

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
  @Input() selectId$: Observable<any>;
  @Input() entityName: string;
  @Output() selectIdChange = new EventEmitter();
  @Output() rename = new EventEmitter();
  constructor() { }

  ngOnInit() {
    // this.selectedId = this.selectId;
    this.selectId$.subscribe(e => this.selectedId = e);
  }

  set selected(value) {
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
    // console.log(this.newName);
    this.rename.emit(this.newName);

    this.editing = false;
  }

  edit() {
    this.editing = true;
    this.newName = this.options.find(option => option.id === this.selectedId).name;
  }
}
