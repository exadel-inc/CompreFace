import {Component, Inject} from '@angular/core';
import {MAT_SNACK_BAR_DATA} from '@angular/material';

@Component({
    selector: 'app-snackbar',
    styleUrls: ['./snackbar.component.sass'],
    templateUrl: './snackbar.component.html'
})
export class AppSnackBarComponent {
    public type: string;
    public message: string;

    constructor(@Inject(MAT_SNACK_BAR_DATA) data: { type: string, message: string }) {
        this.type = data.type;
        this.message = data.message;
    }
}
