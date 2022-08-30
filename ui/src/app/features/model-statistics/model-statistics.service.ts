import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Label } from 'ng2-charts';
import { Statistics } from 'src/app/data/interfaces/statistics';

@Injectable()
export class ModelStatsService {
  constructor(private translate: TranslateService) {}

  getMonthStats(dataArr: Statistics[]) {
    let monthNames = [
      this.translate.instant('month.January'),
      this.translate.instant('month.February'),
      this.translate.instant('month.March'),
      this.translate.instant('month.April'),
      this.translate.instant('month.May'),
      this.translate.instant('month.June'),
      this.translate.instant('month.July'),
      this.translate.instant('month.August'),
      this.translate.instant('month.September'),
      this.translate.instant('month.October'),
      this.translate.instant('month.November'),
      this.translate.instant('month.December'),
    ];
    const newDataArr = dataArr.map(el => {
      const month = new Date(el.createdDate).getMonth();
      return {
        requestCount: el.requestCount,
        createdDate: monthNames[month],
      };
    });

    const currentMonth = new Date().getMonth();

    let newData = [];

    for (let i = currentMonth; i > currentMonth - 6; i--) {
      const index = i < 0 ? monthNames.length + i : i; // check if current month index is a negative number
      const data = newDataArr.filter(el => el.createdDate === monthNames[index]);
      const statData = data.length
        ? data.reduce((el1, el2) => {
            return { createdDate: el1.createdDate, requestCount: el1.requestCount + el2.requestCount };
          })
        : { createdDate: monthNames[index], requestCount: 0 };

      newData.unshift(statData);
    }

    return newData;
  }

  getDays(statsData: Statistics[]): Statistics[] {
    const dayStats = statsData
      .filter(el => el.createdDate)
      .map(el => {
        return {
          requestCount: el.requestCount,
          createdDate: el.createdDate.slice(5, 10).split('-').reverse().join('-'),
        };
      });

    return dayStats.length > 7 ? dayStats.slice(-7) : dayStats;
  }
}
