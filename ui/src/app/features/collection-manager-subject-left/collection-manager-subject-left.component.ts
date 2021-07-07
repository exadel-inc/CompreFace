import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import { CreateDialogComponent } from '../create-dialog/create-dialog.component';
import { TranslateService } from '@ngx-translate/core';
import { MatDialog } from '@angular/material/dialog';
import {CollectionLeftFacade} from "./collection-left-facade";
import {
  selectAddSubject,
  selectAddSubjectPending,
  selectCollectionSubject
} from "../../store/manage-collectiom/selectors";
import {ActivatedRoute} from "@angular/router";
import {Observable} from "rxjs";
import {Store} from "@ngrx/store";

@Component({
  selector: 'app-collection-manager-subject-left',
  templateUrl: './collection-manager-subject-left.component.html',
  styleUrls: ['./collection-manager-subject-left.component.scss'],
})
export class CollectionManagerSubjectLeftComponent implements OnInit{
  @Input() searchPlaceholder: string;
  @Input() subjectsList: string[];

  @Output() inputSearch: EventEmitter<string> = new EventEmitter();

  subjects$: Observable<string[]>;
  isLoading$: Observable<boolean>;

  constructor(private translate: TranslateService, private dialog: MatDialog, private collectionLeftFacade: CollectionLeftFacade, private store: Store<any>) {}

  ngOnInit(): void {
    this.subjects$ = this.store.select(selectAddSubject);
    this.isLoading$ = this.store.select(selectAddSubjectPending);
    this.collectionLeftFacade.initUrlBindingStreams();
  }

  onInputChange(event: Event): void {
    const data = event.target as HTMLInputElement;
    this.inputSearch.emit(data.value);
  }

  openModalWindow() {
    const dialog = this.dialog.open(CreateDialogComponent, {
      panelClass: 'custom-mat-dialog',
      data: {
        entityType: this.translate.instant('manage_collection.left_side.modal_title'),
        name: '',
      },
    });

    const dialogSubscription = dialog.afterClosed().subscribe(name => {
      if (name) {
        this.collectionLeftFacade.addSubject(name);
        dialogSubscription.unsubscribe();
      }
    });
  }
}
