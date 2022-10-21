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
import { Component, Input, OnChanges } from '@angular/core';
import { ChartDataSets, ChartOptions } from 'chart.js';
import { Label, Color } from 'ng2-charts';
import { Statistics } from 'src/app/data/interfaces/statistics';
import { ModelStatsService } from './model-statistics.service';

@Component({
  selector: 'model-statistics',
  templateUrl: './model-statistics.component.html',
  styleUrls: ['./model-statistics.component.scss'],
})
export class ModelStatisticsComponent implements OnChanges {
  @Input() statistics: Statistics[];
  @Input() type: string;

  labels: Label[];

  // Array of different segments in chart
  lineChartData: ChartDataSets[] = [{ data: [] }];

  // Define chart options
  lineChartOptions: ChartOptions = {
    responsive: true,
    legend: {
      labels: {
        fontFamily: "'Poppins', sans-serif",
      },
    },
  };

  // Define colors of chart segments
  lineChartColors: Color[] = [
    {
      backgroundColor: 'rgba(140,151,167,0.5)',
      borderColor: 'rgb(4,35,77)',
    },
  ];

  constructor(private statsService: ModelStatsService) {}

  ngOnChanges(): void {
    this.labels = this.type === 'month' ? this.getMonth(Object.values(this.statistics)) : this.getDays(Object.values(this.statistics));
  }

  getMonth(stats): Label[] {
    const monthStats = this.statsService.getMonthStats(stats);
    this.lineChartData = [{ data: monthStats.map(el => el.requestCount) }];
    return monthStats.map(el => el.createdDate);
  }

  getDays(stats: Statistics[]): Label[] {
    const dayStats = this.statsService.getDays(stats);
    this.lineChartData = [{ data: dayStats.map(el => el.requestCount) }];
    return dayStats.map(el => el.createdDate);
  }
}
