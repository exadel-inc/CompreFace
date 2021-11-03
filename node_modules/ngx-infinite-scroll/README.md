[![Build Status](https://travis-ci.org/orizens/ngx-infinite-scroll.svg?branch=master)](https://travis-ci.org/orizens/ngx-infinite-scroll) [![Backers on Open Collective](https://opencollective.com/ngx-infinite-scroll/backers/badge.svg)](#backers) [![Sponsors on Open Collective](https://opencollective.com/ngx-infinite-scroll/sponsors/badge.svg)](#sponsors)
[![npm version](https://badge.fury.io/js/ngx-infinite-scroll.svg)](https://badge.fury.io/js/ngx-infinite-scroll)
[![npm version](https://badge.fury.io/js/ngx-infinite-scroll.svg)](https://badge.fury.io/js/ngx-infinite-scroll)
[![npm downloads a month](https://img.shields.io/npm/dm/ngx-infinite-scroll.svg)](https://img.shields.io/npm/dm/ngx-infinite-scroll.svg)
[![npm downloads a week](https://img.shields.io/npm/dt/ngx-infinite-scroll.svg)](https://img.shields.io/npm/dt/ngx-infinite-scroll.svg)

## [Consider Becoming a sponsor](https://opencollective.com/ngx-infinite-scroll#sponsor)

# Angular Infinite Scroll

versions now follow Angular's version to easily reflect compatibility.  
Meaning, for **Angular 10**, use `ngx-infinite-scroll @ ^10.0.0`

## Angular - Older Versions Support

Starting **Angular 6 and Above** - `ngx-infinite-scroll@THE_VERSION.0.0`  
For **Angular 4** and **Angular = ^5.5.6** - use version `ngx-infinite-scroll@0.8.4`  
For **Angular 5.x** with **rxjs =<5.5.2** - use version `ngx-infinite-scroll@0.8.3`  
For Angular version **<= 2.3.1**, you can use `npm i angular2-infinite-scroll` (latest version is 0.3.42) - please notice **the angular2-infinite-scroll** package is deprecated

## Used By

- [Google](https://google.com)
- [Apple](https://apple.com)
- [Amazon](https://amazon.com)
- [Microsoft](https://microsoft.com)
- [Disney](https://disney.com)
- [Sap](https://sap.com/)
- [Cisco](https://cisco.com/)
- [Yandex](https://yandex.com)
- [Ancestry](https://www.ancestry.com/)

and much more.

> _These analytics are made available via the awesome [Scarf](https://www.npmjs.com/package/@scarf/scarf) package analytics library_

## Front End Consulting Services

I'm a Senior Front End Engineer & Consultant at [Orizens](https://orizens.com).
My services include:

- Angular/React/Javascript Consulting
- Front End Architecture Consulting
- Project Code Review
- Project Development

[Contact Here](http://orizens.com/contact)

<a href="https://orizens.com" target="_blank">
  <img src="https://cloud.githubusercontent.com/assets/878660/23353771/d0adbd12-fcd6-11e6-96be-7a236f8819d9.png" alt="Webpack and Angular" width="20%"/>
</a>

## Installation

```
npm install ngx-infinite-scroll --save
```

## Supported API

### Properties

| @Input()                 | Type                 | Required | Default | Description                                                                                                                                                                                                                                                                                           |
| ------------------------ | -------------------- | -------- | ------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| infiniteScrollDistance   | number               | optional | 2       | the bottom percentage point of the scroll nob relatively to the infinite-scroll container (i.e, 2 (2 \* 10 = 20%) is event is triggered when 80% (100% - 20%) has been scrolled). if container.height is 900px, when the container is scrolled to or past the 720px, it will fire the scrolled event. |
| infiniteScrollUpDistance | number               | optional | 1.5     | should get a number                                                                                                                                                                                                                                                                                   |
| infiniteScrollThrottle   | number               | optional | 150     | should get a number of **milliseconds** for throttle. The event will be triggered this many milliseconds after the user _stops_ scrolling.                                                                                                                                                            |
| scrollWindow             | boolean              | optional | true    | listens to the window scroll instead of the actual element scroll. this allows to invoke a callback function in the scope of the element while listenning to the window scroll.                                                                                                                       |
| immediateCheck           | boolean              | optional | false   | invokes the handler immediately to check if a scroll event has been already triggred when the page has been loaded (i.e. - when you refresh a page that has been scrolled)                                                                                                                            |
| infiniteScrollDisabled   | boolean              | optional | false   | doesn't invoke the handler if set to true                                                                                                                                                                                                                                                             |
| horizontal               | boolean              | optional | false   | sets the scroll to listen for horizontal events                                                                                                                                                                                                                                                       |
| alwaysCallback           | boolean              | optional | false   | instructs the scroller to always trigger events                                                                                                                                                                                                                                                       |
| infiniteScrollContainer  | string / HTMLElement | optional | null    | should get a html element or css selector for a scrollable element; window or current element will be used if this attribute is empty.                                                                                                                                                                |
| fromRoot                 | boolean              | optional | false   | if **infiniteScrollContainer** is set, this instructs the scroller to query the container selector from the root of the **document** object.                                                                                                                                                          |

### Events

| @Output()  | Type         | Event Type           | Required | Description                                                                     |
| ---------- | ------------ | -------------------- | -------- | ------------------------------------------------------------------------------- |
| scrolled   | EventEmitter | IInfiniteScrollEvent | optional | this will callback if the distance threshold has been reached on a scroll down. |
| scrolledUp | EventEmitter | IInfiniteScrollEvent | optional | this will callback if the distance threshold has been reached on a scroll up.   |

## Behavior

By default, the directive listens to the **window scroll** event and invoked the callback.  
**To trigger the callback when the actual element is scrolled**, these settings should be configured:

- [scrollWindow]="false"
- set an explict css "height" value to the element

## DEMO

[Try the Demo in StackBlitz](https://stackblitz.com/edit/ngx-infinite-scroll)

## Usage

First, import the InfiniteScrollModule to your module:

```typescript
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { AppComponent } from './app';

@NgModule({
  imports: [BrowserModule, InfiniteScrollModule],
  declarations: [AppComponent],
  bootstrap: [AppComponent],
})
export class AppModule {}

platformBrowserDynamic().bootstrapModule(AppModule);
```

In this example, the **onScroll** callback will be invoked when the window is scrolled down:

```typescript
import { Component } from '@angular/core';

@Component({
  selector: 'app',
  template: `
    <div
      class="search-results"
      infiniteScroll
      [infiniteScrollDistance]="2"
      [infiniteScrollThrottle]="50"
      (scrolled)="onScroll()"
    ></div>
  `,
})
export class AppComponent {
  onScroll() {
    console.log('scrolled!!');
  }
}
```

in this example, whenever the "search-results" is scrolled, the callback will be invoked:

```typescript
import { Component } from '@angular/core';

@Component({
  selector: 'app',
  styles: [
    `
      .search-results {
        height: 20rem;
        overflow: scroll;
      }
    `,
  ],
  template: `
    <div
      class="search-results"
      infiniteScroll
      [infiniteScrollDistance]="2"
      [infiniteScrollThrottle]="50"
      (scrolled)="onScroll()"
      [scrollWindow]="false"
    ></div>
  `,
})
export class AppComponent {
  onScroll() {
    console.log('scrolled!!');
  }
}
```

In this example, the **onScrollDown** callback will be invoked when the window is scrolled down and the **onScrollUp** callback will be invoked when the window is scrolled up:

```typescript
import { Component } from '@angular/core';
import { InfiniteScroll } from 'ngx-infinite-scroll';

@Component({
  selector: 'app',
  directives: [InfiniteScroll],
  template: `
    <div
      class="search-results"
      infiniteScroll
      [infiniteScrollDistance]="2"
      [infiniteScrollUpDistance]="1.5"
      [infiniteScrollThrottle]="50"
      (scrolled)="onScrollDown()"
      (scrolledUp)="onScrollUp()"
    ></div>
  `,
})
export class AppComponent {
  onScrollDown() {
    console.log('scrolled down!!');
  }

  onScrollUp() {
    console.log('scrolled up!!');
  }
}
```

In this example, the **infiniteScrollContainer** attribute is used to point directive to the scrollable container using a css selector. **fromRoot** is used to determine whether the scroll container has to be searched within the whole document (`[fromRoot]="true"`) or just inside the **infiniteScroll** directive (`[fromRoot]="false"`, default option).

```typescript
import { Component } from '@angular/core';

@Component({
  selector: 'app',
  styles: [
    `
      .main-panel {
        height: 100px;
        overflow-y: scroll;
      }
    `,
  ],
  template: `
    <div class="main-panel">
      <div
        infiniteScroll
        [infiniteScrollDistance]="2"
        [infiniteScrollThrottle]="50"
        [infiniteScrollContainer]="selector"
        [fromRoot]="true"
        (scrolled)="onScroll()"
      ></div>
    </div>
  `,
})
export class AppComponent {
  selector: string = '.main-panel';

  onScroll() {
    console.log('scrolled!!');
  }
}
```

It is also possible to use **infiniteScrollContainer** without additional variable by using single quotes inside double quotes:

```
[infiniteScrollContainer]="'.main-panel'"
```

# Showcase Examples

- [Echoes Player - Developed with Angular, angular-cli and ngrx](http://orizens.github.io/echoes-player) ([github repo for echoes player](http://github.com/orizens/echoes-player))

## Contributors

This project exists thanks to all the people who contribute. [[Contribute](CONTRIBUTING.md)].
<a href="graphs/contributors"><img src="https://opencollective.com/ngx-infinite-scroll/contributors.svg?width=890" /></a>

## Backers

Thank you to all our backers! 🙏 [[Become a backer](https://opencollective.com/ngx-infinite-scroll#backer)]

<a href="https://opencollective.com/ngx-infinite-scroll#backers" target="_blank"><img src="https://opencollective.com/ngx-infinite-scroll/backers.svg?width=890"></a>

## Sponsors

<a href="https://opencollective.com/ngx-infinite-scroll/sponsor/0/website" target="_blank"><img src="https://opencollective.com/ngx-infinite-scroll/sponsor/0/avatar.svg"></a>
<a href="https://opencollective.com/ngx-infinite-scroll/sponsor/1/website" target="_blank"><img src="https://opencollective.com/ngx-infinite-scroll/sponsor/1/avatar.svg"></a>
<a href="https://opencollective.com/ngx-infinite-scroll/sponsor/2/website" target="_blank"><img src="https://opencollective.com/ngx-infinite-scroll/sponsor/2/avatar.svg"></a>
<a href="https://opencollective.com/ngx-infinite-scroll/sponsor/3/website" target="_blank"><img src="https://opencollective.com/ngx-infinite-scroll/sponsor/3/avatar.svg"></a>
<a href="https://opencollective.com/ngx-infinite-scroll/sponsor/4/website" target="_blank"><img src="https://opencollective.com/ngx-infinite-scroll/sponsor/4/avatar.svg"></a>
<a href="https://opencollective.com/ngx-infinite-scroll/sponsor/5/website" target="_blank"><img src="https://opencollective.com/ngx-infinite-scroll/sponsor/5/avatar.svg"></a>
<a href="https://opencollective.com/ngx-infinite-scroll/sponsor/6/website" target="_blank"><img src="https://opencollective.com/ngx-infinite-scroll/sponsor/6/avatar.svg"></a>
<a href="https://opencollective.com/ngx-infinite-scroll/sponsor/7/website" target="_blank"><img src="https://opencollective.com/ngx-infinite-scroll/sponsor/7/avatar.svg"></a>
<a href="https://opencollective.com/ngx-infinite-scroll/sponsor/8/website" target="_blank"><img src="https://opencollective.com/ngx-infinite-scroll/sponsor/8/avatar.svg"></a>
<a href="https://opencollective.com/ngx-infinite-scroll/sponsor/9/website" target="_blank"><img src="https://opencollective.com/ngx-infinite-scroll/sponsor/9/avatar.svg"></a>
