import { NgModule } from '@angular/core';
import { AppSnackBar } from './snackbar.component';
import { MatSnackBarModule } from '@angular/material'
import { CommonModule } from '@angular/common';

@NgModule({
    declarations: [AppSnackBar],
    imports: [MatSnackBarModule, CommonModule],
    entryComponents: [AppSnackBar]
})
export class SnackBarModule {}
