import { TranslateLoader } from '@ngx-translate/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

interface Translation {
  [key: string]: string | Translation;
}

export class NoCacheTranslateLoader implements TranslateLoader {
  constructor(private http: HttpClient) {}

  getTranslation(lang: string): Observable<Translation> {
    return this.http.get<Translation>(`/assets/i18n/${lang}.json?t=${Date.now()}`).pipe(
      catchError(() => {
        console.error(`Problem with '${lang}' language initialization.'`);
        return of({});
      })
    );
  }
}
