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

  tmpStats = [
    {
      requestCount: 1,
      createdDate: '2022-01-14T21:00:00',
    },
    {
      requestCount: 1,
      createdDate: '2022-02-14T21:00:00',
    },
    {
      requestCount: 1,
      createdDate: '2022-03-14T21:00:00',
    },
    {
      requestCount: 1,
      createdDate: '2022-04-14T21:00:00',
    },
    {
      requestCount: 1,
      createdDate: '2022-05-14T21:00:00',
    },
    {
      requestCount: 5,
      createdDate: '2022-05-15T21:00:00',
    },
    {
      requestCount: 3,
      createdDate: '2022-05-16T21:00:00',
    },
    {
      requestCount: 21,
      createdDate: '2022-06-14T21:00:00',
    },
    {
      requestCount: 17,
      createdDate: '2022-07-15T21:00:00',
    },
    {
      requestCount: 33,
      createdDate: '2022-08-16T21:00:00',
    },
  ];

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
    this.labels = this.type === 'month' ? this.getMonth(this.tmpStats) : this.getDays(this.tmpStats);
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

    for (let i = 6; i > 0; i -= 1) {
      date = new Date(today.getFullYear(), today.getMonth() - i, 1);
      month.push(date.getMonth() + 1);
    }

    this.lineChartData = [{ data: this.getMonthStats(stats).map(el => el.requestCount) }];

    // getting last 6 month
    return monthNames.slice(month[0], month[0] + 6);
  }

  getMonthStats(dataArr: Statistics[]) {
    let newData = [];
    for (let i = 0; i < dataArr.length - 1; i++) {
      const day1 = new Date(dataArr[i].createdDate).getMonth();
      const day2 = new Date(dataArr[i + 1].createdDate).getMonth();
      console.log(day1, day2);
      if (day1 === day2) {
        // searchs if the same month is repeated multiple times
        newData.push({ requestCount: dataArr[i].requestCount + dataArr[i + 1].requestCount, createdDate: dataArr[i].createdDate });
        i++;
      } else {
        newData.push(dataArr[i]);
      }
    }

    console.log(newData);
    return newData.slice(newData.length - 7);
  }

  getDays(stats: Statistics[]): Label[] {
    const statData = Object.values(stats);
    let data = statData.map(el => el.requestCount).slice(statData.length - 7);
    this.lineChartData = [{ data: data }];

    const dayStats = statData.filter(el => el.createdDate).map(el => el.createdDate.slice(5, 10).split('-').reverse().join('-'));

    if (dayStats.length < 7) return dayStats;

    return dayStats.slice(dayStats.length - 7);
  }
}
