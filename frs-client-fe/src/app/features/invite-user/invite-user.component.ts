import { ChangeDetectionStrategy, Component, OnInit, Output, EventEmitter } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { EMAIL_REGEXP_PATTERN } from 'src/app/core/constants';

@Component({
  selector: 'app-invite-user',
  templateUrl: './invite-user.component.html',
  styleUrls: ['./invite-user.component.sass'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InviteUserComponent implements OnInit {
  public form: FormGroup;
  @Output() onChange = new EventEmitter<string>();

  constructor() { }

  ngOnInit() {
    this.form = new FormGroup({
      email: new FormControl(null, [Validators.pattern(EMAIL_REGEXP_PATTERN)])
    });
  }

  public onClick(): void {
    if (this.form.valid && this.form.value.email && this.form.value.email.length) {
      this.onChange.emit(this.form.value.email);
      this.form.reset();
    }
  }
}
