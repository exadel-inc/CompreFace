import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material';
import { AppSnackBarComponent } from './snackbar.component';

const messageMap = {
    'default-info': 'DEFAULT INFO MESSAGE',
    'default-error': 'DEFAULT ERROR MESSAGE'
};

@Injectable({
    providedIn: 'root'
})
export class SnackBarService {
    constructor(private snackBar: MatSnackBar) {}

    public openInfo(messageCode: string, duration: number = 3000, message?: string): void {
        const data = {
            message: '',
            type: 'info'
        };

        data.message = messageCode ? messageMap[messageCode] : message;
        this.openSnackBar(data, duration);
    }

    public openError(messageCode: string, duration: number = 8000, message?: string): void {
        const data = {
            message: '',
            type: 'error'
        };

        data.message = messageCode ? messageMap[messageCode] : message;
        this.openSnackBar(data, duration);
    }

    public openHttpError(message: HttpErrorResponse): void {
        const data = {
            message: message.error.message,
            type: 'error'
        };

        const duration: number = 4000;

        data.message = message.error.message || message.message;
        this.openSnackBar(data, duration);
    }

    private openSnackBar(data, duration): void {
        this.snackBar.openFromComponent(AppSnackBarComponent, {
            duration,
            data,
            verticalPosition: 'top',
            panelClass: ['app-snackbar-panel', data.type]
        });
    }
}
