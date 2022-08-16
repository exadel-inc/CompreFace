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

  ngOnInit(): void {
    this.labels = this.type === 'month' ? this.getMonth(Object.values(this.statistics)) : this.getDays(Object.values(this.statistics));
  }

  getMonth(stats): Label[] {
    let monthNames = [
      'January',
      'February',
      'March',
      'April',
      'May',
      'June',
      'July',
      'August',
      'September',
      'October',
      'November',
      'December',
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
      if (day1?.getMonth() === day2?.getMonth()) {
        // searchs if the same month is repeated multiple times
        newData.push({ requestCount: dataArr[i].requestCount + dataArr[i + 1].requestCount, createdDate: dataArr[i].createdDate });
        i++;
      } else {
        newData.push(dataArr[i]);
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
