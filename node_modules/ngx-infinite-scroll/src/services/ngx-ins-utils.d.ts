import { ElementRef, SimpleChange } from '@angular/core';
export declare function resolveContainerElement(selector: string | any, scrollWindow: any, defaultElement: any, fromRoot: boolean): any;
export declare function findElement(selector: string | any, customRoot: ElementRef | any, fromRoot: boolean): any;
export declare function inputPropChanged(prop: SimpleChange): boolean;
export declare function hasWindowDefined(): boolean;
