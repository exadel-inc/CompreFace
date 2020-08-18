import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'truncate'
})
export class TruncatePipe implements PipeTransform {

  transform(value: string, limit: number): any {
    const elipses = "...";

    if(typeof value === "undefined") return value;
    if(value.length <= limit) return value;
    let truncatedValue = value.slice(0, limit)
    return truncatedValue + elipses;
  }

}
