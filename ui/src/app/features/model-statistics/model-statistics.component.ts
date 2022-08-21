/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import { Component, Input } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ChartDataSets, ChartOptions } from 'chart.js';
import { Label, Color } from 'ng2-charts';
import { Statistics } from 'src/app/data/interfaces/statistics';

@Component({
  selector: 'model-statistics',
  templateUrl: './model-statistics.component.html',
  styleUrls: ['./model-statistics.component.scss'],
})
export class ModelStatisticsComponent {
  @Input() statistics: Statistics[];
  @Input() type: string;

  labels: Label[];

  // Array of different segments in chart
  lineChartData: ChartDataSets[] = [{ data: [] }];

  // Define chart options
  lineChartOptions: ChartOptions = {
    responsive: true,
  };

  // Define colors of chart segments
  lineChartColors: Color[] = [
    {
      backgroundColor: 'rgba(140,151,167,0.5)',
      borderColor: 'rgb(4,35,77)',
    },
  ];

  constructor(private translate: TranslateService) {}

  ngOnInit(): void {
    this.labels = this.type === 'month' ? this.getMonth(Object.values(this.statistics)) : this.getDays(Object.values(this.statistics));
  }

  getMonth(stats): Label[] {
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

    let today = new Date();
    let date;
    let month = [];
    this.lineChartData = [{ data: this.getMonthStats(stats).map(el => el.requestCount) }];

    for (let i = this.lineChartData[0].data.length; i > 0; i -= 1) {
      date = new Date(today.getFullYear(), today.getMonth() - i, 1);
      month.push(date.getMonth() + 1);
    }

    // getting months => max last 6 monthes
    return monthNames.slice(month[0], month[month.length - 1] + 1);
  }

  getMonthStats(dataArr: Statistics[]) {
    let newData = [];
    for (let i = 0; i < dataArr.length; i++) {
      const day1 = new Date(dataArr[i]?.createdDate);
      const day2 = new Date(dataArr[i + 1]?.createdDate);
      // if the same month is repeated multiple times in dataArr
      if (day1?.getMonth() === day2?.getMonth()) {
        const lastDate = new Date(newData[newData.length - 1]?.createdDate);
        // if month already exists in the newData array
        if (lastDate.getMonth() === day1.getMonth()) {
          newData[newData.length - 1].requestCount = newData[newData.length - 1].requestCount + dataArr[i + 1].requestCount;
        } else {
          newData.push({ requestCount: dataArr[i].requestCount + dataArr[i + 1].requestCount, createdDate: dataArr[i + 1].createdDate });
        }
      } else {
        // if next date value is valid and does not equel to 0 (January)
        if (day2.getMonth() && day2.getMonth() !== 0) {
          newData.push(dataArr[i]);
        }
      }
    }

    return newData.length > 6 ? newData.slice(-7) : newData;
  }

  getDays(stats: Statistics[]): Label[] {
    const statData = Object.values(stats);
    let data = statData.map(el => el.requestCount).slice(statData.length - 7);
    this.lineChartData = [{ data: data }];

    const dayStats = statData.filter(el => el.createdDate).map(el => el.createdDate.slice(5, 10).split('-').reverse().join('-'));

    if (dayStats.length < 7) return dayStats;

    return dayStats.length > 7 ? dayStats.slice(-7) : dayStats;
  }
}
