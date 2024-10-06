import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Statistics } from 'src/app/data/interfaces/statistics';

interface Month {
  name: string;
  maxDays: number;
}

@Injectable()
export class ModelStatsService {
  months: Month[] = [
    {
      name: this.translate.instant('month.January'),
      maxDays: 31,
    },
    {
      name: this.translate.instant('month.February'),
      maxDays: 29,
    },
    {
      name: this.translate.instant('month.March'),
      maxDays: 31,
    },
    {
      name: this.translate.instant('month.April'),
      maxDays: 30,
    },
    {
      name: this.translate.instant('month.May'),
      maxDays: 31,
    },
    {
      name: this.translate.instant('month.June'),
      maxDays: 30,
    },
    {
      name: this.translate.instant('month.July'),
      maxDays: 31,
    },
    {
      name: this.translate.instant('month.August'),
      maxDays: 31,
    },
    {
      name: this.translate.instant('month.September'),
      maxDays: 30,
    },
    {
      name: this.translate.instant('month.October'),
      maxDays: 31,
    },
    {
      name: this.translate.instant('month.November'),
      maxDays: 30,
    },
    {
      name: this.translate.instant('month.December'),
      maxDays: 31,
    },
  ];

  constructor(private translate: TranslateService) {}

  getMonthStats(dataArr: Statistics[]) {
    const newDataArr = dataArr.slice(0, dataArr.length - 1).map(el => {
      const month = new Date(el.createdDate).getMonth();
      return {
        requestCount: el.requestCount,
        createdDate: this.months[month].name,
      };
    });

    const currentMonth = new Date().getMonth();
    const maxMonthDisplayed = 6;

    let newData = [];

    for (let i = currentMonth; i > currentMonth - maxMonthDisplayed; i--) {
      const index = i < 0 ? this.months.length + i : i; // check if current month index is a negative number
      const data = newDataArr.filter(el => el.createdDate === this.months[index].name);
      const statData = data.length
        ? data.reduce((el1, el2) => {
            return { createdDate: el1.createdDate, requestCount: el1.requestCount + el2.requestCount };
          })
        : { createdDate: this.months[index].name, requestCount: 0 };

      newData.unshift(statData);
    }

    return newData;
  }

  getDays(statsData: Statistics[]): Statistics[] {
    const now = new Date();
    const currentData = new Date().getDate();
    const month = new Date().getMonth();
    let lastThirtyDaysArr = [];

    // gets day-month array
    for (let i = 0; i < 30; i++) {
      const currentMonthDate = new Date().getDate() - i;
      if (currentMonthDate < 1) {
        const date = this.months[month - 1].maxDays + currentMonthDate;
        const monthData = month.toString().length > 1 ? month.toString() : '0' + month;

        lastThirtyDaysArr.push(date.toString().length > 1 ? `${date}-${monthData}` : `0${date}-${monthData}`);
      } else {
        const currentMonth = month + 1;
        const monthData = currentMonth.toString().length > 1 ? currentMonth.toString() : '0' + currentMonth;

        lastThirtyDaysArr.push(
          currentMonthDate.toString().length > 1 ? `${currentMonthDate}-${monthData}` : `0${currentMonthDate}-${monthData}`
        );
      }
    }

    // gets requestCount and createdDate data array
    const dayStats = statsData
      .filter(el => el.createdDate)
      .map(el => {
        return {
          requestCount: el.requestCount,
          createdDate: el.createdDate.slice(5, 10).split('-').reverse().join('-'),
        };
      });

    // gets final result to display in charts
    const daysStatsData = lastThirtyDaysArr.map(el => {
      const filteredDatesArr = dayStats.filter(day => el == day.createdDate);

      if (filteredDatesArr.length > 0) {
        const finalResult = filteredDatesArr.reduce((el1, el2) => {
          return {
            requestCount: el1.requestCount + el2.requestCount,
            createdDate: el1.createdDate,
          };
        });

        return finalResult;
      } else {
        return { requestCount: 0, createdDate: el };
      }
    });

    return daysStatsData.reverse();
  }
}
