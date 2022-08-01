import { Component } from '@angular/core';
import { ChartConfiguration, ChartOptions } from 'chart.js';

@Component({
  selector: 'model-statistics',
  templateUrl: './model-statistics.component.html',
  styleUrls: ['./model-statistics.component.scss'],
})
export class ModelStatisticsComponent {
  public lineChartData: ChartConfiguration['data'] = {
    labels: ['January', 'February', 'March', 'April', 'May', 'June', 'July'],
    datasets: [
      {
        data: [65, 59, 80, 81, 56, 55, 40],
        label: 'Series A',
        fill: true,
      },
    ],
  };
  public lineChartOptions: ChartOptions = {
    responsive: false,
  };
  public lineChartLegend = true;
}
