import { IScrollState, IScrollerDistance } from '../models';
export declare class ScrollState implements IScrollState {
    lastScrollPosition: number;
    lastTotalToScroll: number;
    totalToScroll: number;
    triggered: IScrollerDistance;
    constructor({ totalToScroll }: {
        totalToScroll: any;
    });
    updateScrollPosition(position: number): number;
    updateTotalToScroll(totalToScroll: number): void;
    updateScroll(scrolledUntilNow: number, totalToScroll: number): void;
    updateTriggeredFlag(scroll: any, isScrollingDown: boolean): void;
    isTriggeredScroll(totalToScroll: any, isScrollingDown: boolean): boolean;
}
