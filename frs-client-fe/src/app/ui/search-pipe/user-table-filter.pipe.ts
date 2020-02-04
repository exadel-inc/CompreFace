import {Pipe, PipeTransform} from "@angular/core";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";

@Pipe({
  name: 'userTableFilter'
})
export class UserTableFilterPipe implements PipeTransform{
  transform(value: Observable<any>, search: string): Observable<any> {
    if(!search.trim()) return value;

    return value.pipe(
      map(e => {
        e.data = e.data.filter(row => {
          return (row.firstName.toLocaleLowerCase()+ ' ' + row.lastName.toLocaleLowerCase()).includes(search.toLocaleLowerCase())
        });
        return e;
      })
    )
  }

}
