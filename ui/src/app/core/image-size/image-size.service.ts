import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { MaxImageSize } from 'src/app/data/interfaces/size.interface';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ImageSizeService {
  constructor(private http: HttpClient) {}

  fetchMaxSize(): Observable<MaxImageSize> {
    return this.http.get<MaxImageSize>(`${environment.userApiUrl}config`);
  }
}
