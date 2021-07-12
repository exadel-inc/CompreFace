import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'subjectFilter',
})
export class SubjectFilter implements PipeTransform {
  transform(value, search: string) {
    if (!search.trim()) {
      return value;
    }
    let result = value;
    result = result.filter((subject: string) => subject.toLocaleLowerCase().includes(search.toLocaleLowerCase()));
    return result;
  }
}
