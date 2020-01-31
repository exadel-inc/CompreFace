import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Organization} from "../../data/organization";
import {Observable} from "rxjs";
import {Application} from "../../data/application";
import {Model} from "../../data/model";

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
  @Input() options: [Organization | Application | Model];
  @Input() renameDisable: boolean;
  @Input() selectId$: Observable<any>;
  @Input() entityName: string;
  @Output() selectIdChange = new EventEmitter();
  @Output() rename = new EventEmitter();

  ngOnInit() {
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
    this.rename.emit(this.newName);

    this.editing = false;
  }

  edit() {
    this.editing = true;
    this.newName = this.options.find(option => option.id === this.selectedId).name;
  }
}
