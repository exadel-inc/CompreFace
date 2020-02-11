import {NgModule} from '@angular/core';
import {AppSnackBarComponent} from './snackbar.component';
import {MatSnackBarModule} from '@angular/material';
import {CommonModule} from '@angular/common';

@NgModule({
    declarations: [AppSnackBarComponent],
    imports: [MatSnackBarModule, CommonModule],
    entryComponents: [AppSnackBarComponent]
})
export class SnackBarModule {}
