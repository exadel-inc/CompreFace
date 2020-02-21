import {Component, OnInit, Inject, Input, ChangeDetectionStrategy} from '@angular/core';
import {FormGroup, FormControl, Validators, FormBuilder} from '@angular/forms';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';
import {EMAIL_REGEXP_PATTERN} from 'src/app/core/constants';
import {Observable, combineLatest} from 'rxjs';
import {startWith, map} from 'rxjs/operators';

@Component({
  selector: 'app-invite-dialog',
  templateUrl: './invite-dialog.component.html',
  styleUrls: ['./invite-dialog.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InviteDialogComponent implements OnInit {
  public availableRoles: string[];
  public form: FormGroup;
  public users: string[];
  public filteredOptions$: Observable<string[]>;
  public actionType: string;

  constructor(
    private formBuilder: FormBuilder,
    public dialogRef: MatDialogRef<InviteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.availableRoles = this.data.availableRoles;
    this.actionType = this.data.actionType || 'invite';
  }

  ngOnInit() {
    this.form = this.formBuilder.group({
      userEmail: new FormControl(null, [Validators.required, Validators.pattern(EMAIL_REGEXP_PATTERN)]),
      role: new FormControl(this.availableRoles[0], [Validators.required])
    });

    if (this.data.options$) {
      this.filteredOptions$ = combineLatest(
        this.data.options$,
        this.form.controls.userEmail.valueChanges.pipe(startWith(''))
      ).pipe(
        map(([options, value]) => this.filter(options as string[], value)),
      );
    }
  }

  public onCancelClick(): void {
    this.dialogRef.close({});
  }

  public onInviteClick(): void {
    if (this.form.valid) {
      this.dialogRef.close({
        ...this.form.value
      });
    }
  }

  private filter(options: string[], value: string): string[] {
    const filterValue = value ? value.toLowerCase() : '';
    return options ? options.filter(option => option && option.toLowerCase().indexOf(filterValue) === 0) : [''];
  }
}
