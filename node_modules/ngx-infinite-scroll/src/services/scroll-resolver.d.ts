import { IPositionStats, IScrollState, IScrollerDistance } from '../models';
export declare function shouldFireScrollEvent(container: IPositionStats, distance: IScrollerDistance, scrollingDown: boolean): boolean;
export declare function isScrollingDownwards(lastScrollPosition: number, container: IPositionStats): boolean;
export declare function getScrollStats(lastScrollPosition: number, container: IPositionStats, distance: IScrollerDistance): {
    fire: boolean;
    scrollDown: boolean;
};
export declare function updateScrollPosition(position: number, scrollState: IScrollState): number;
export declare function updateTotalToScroll(totalToScroll: number, scrollState: IScrollState): void;
export declare function isSameTotalToScroll(scrollState: IScrollState): boolean;
export declare function updateTriggeredFlag(scroll: any, scrollState: IScrollState, triggered: boolean, isScrollingDown: boolean): void;
export declare function isTriggeredScroll(totalToScroll: any, scrollState: IScrollState, isScrollingDown: boolean): boolean;
export declare function updateScrollState(scrollState: IScrollState, scrolledUntilNow: number, totalToScroll: number): void;
