import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {OrganizationService} from './organization.service';

@Component({
  selector: 'app-organization',
  templateUrl: './organization.component.html',
  styleUrls: ['./organization.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrganizationComponent implements OnInit, OnDestroy {

  constructor( private organizationService: OrganizationService) {}

  ngOnInit() {
    this.organizationService.initUrlBindingStreams();
  }

  ngOnDestroy() {
    this.organizationService.unSubscribe();
  }
}
