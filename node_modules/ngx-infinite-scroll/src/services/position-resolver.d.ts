import { ElementRef } from '@angular/core';
import { ContainerRef, IPositionElements, IPositionStats, IResolver } from '../models';
import { AxisResolver } from './axis-resolver';
export declare function createResolver({ windowElement, axis }: IPositionElements): IResolver;
export declare function createResolverWithContainer(resolver: any, windowElement: ContainerRef): any;
export declare function isElementWindow(windowElement: ContainerRef): boolean;
export declare function getDocumentElement(isContainerWindow: boolean, windowElement: any): any;
export declare function calculatePoints(element: ElementRef, resolver: IResolver): IPositionStats;
export declare function calculatePointsForWindow(height: number, element: ElementRef, resolver: IResolver): IPositionStats;
export declare function calculatePointsForElement(height: number, element: ElementRef, resolver: IResolver): IPositionStats;
export declare function extractHeightPropKeys(axis: AxisResolver): {
    offsetHeightKey: any;
    clientHeightKey: any;
};
export declare function extractHeightForElement({ container, isWindow, axis }: IResolver): any;
export declare function getElementHeight(elem: any, isWindow: boolean, offsetHeightKey: string, clientHeightKey: string): any;
export declare function getElementOffsetTop(elem: ContainerRef, axis: AxisResolver, isWindow: boolean): any;
export declare function getElementPageYOffset(elem: ContainerRef, axis: AxisResolver, isWindow: boolean): any;
