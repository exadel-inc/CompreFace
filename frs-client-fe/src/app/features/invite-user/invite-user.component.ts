import {ChangeDetectionStrategy, Component, OnInit, Output, EventEmitter, Input} from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { EMAIL_REGEXP_PATTERN } from 'src/app/core/constants';
import {Observable} from "rxjs";
import {map, startWith} from "rxjs/operators";

@Component({
  selector: 'app-invite-user',
  templateUrl: './invite-user.component.html',
  styleUrls: ['./invite-user.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InviteUserComponent implements OnInit {
  public form: FormGroup;
  public control = new FormControl();
  @Input() options: string[];
  filteredOptions: Observable<string[]>;
  @Output() onChange = new EventEmitter<string>();

  constructor() { }

  ngOnInit() {
    this.form = new FormGroup({
      email: new FormControl(null, [Validators.pattern(EMAIL_REGEXP_PATTERN)])
    });

    this.filteredOptions = this.control.valueChanges.pipe(
      startWith(''),
      map(value => this.filter(value))
    );
  }

  public onClick(): void {
    if (this.form.valid && this.form.value.email && this.form.value.email.length) {
      this.onChange.emit(this.form.value.email);
      this.form.reset();
    }
  }

  private filter(value: string): string[] {
    const filterValue = value.toLowerCase();

    return this.options ? this.options.filter(option => option.toLowerCase().indexOf(filterValue) === 0) : [""];
  }
}
