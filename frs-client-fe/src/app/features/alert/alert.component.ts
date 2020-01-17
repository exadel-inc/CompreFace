import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

export interface IAlertData {
  type: 'error' | 'warning' | 'info';
  message: string;
}

@Component({
  selector: 'app-alert',
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.sass']
})
export class AlertComponent implements OnInit {
  public message: string;
  public type: 'error' | 'warning' | 'info';

  constructor(
    public dialogRef: MatDialogRef<AlertComponent>,
    @Inject(MAT_DIALOG_DATA) public data: IAlertData
  ) {
    this.message = data.message;
    this.type = data.type;
  }

  ngOnInit() {}

  public onOkClick(): void {
    this.dialogRef.close();
  }
}
