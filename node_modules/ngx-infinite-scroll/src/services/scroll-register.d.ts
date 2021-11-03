import { Observable } from 'rxjs';
import * as Models from '../models';
export declare function createScroller(config: Models.IScroller): Observable<Models.IInfiniteScrollAction>;
export declare function attachScrollEvent(options: Models.IScrollRegisterConfig): Observable<{}>;
export declare function toInfiniteScrollParams(lastScrollPosition: number, stats: Models.IPositionStats, distance: Models.IScrollerDistance): Models.IScrollParams;
export declare const InfiniteScrollActions: {
    DOWN: string;
    UP: string;
};
export declare function toInfiniteScrollAction(response: Models.IScrollParams): Models.IInfiniteScrollAction;
