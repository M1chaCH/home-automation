import {Component, HostListener} from '@angular/core';
import {AlarmService} from "../../services/alarm.service";

@Component({
  selector: 'app-alarm.page',
  templateUrl: './alarm.page.component.html',
  styleUrls: ['./alarm.page.component.scss']
})
export class AlarmPageComponent {
  mobileView: boolean = true;

  private readonly MOBILE_BREAKPOINT = 900;

  constructor(
    public alarmService: AlarmService,
  ) {
    this.onWindowResize();
  }

  @HostListener("window:resize")
  onWindowResize(): void {
    this.mobileView = window.innerWidth <= this.MOBILE_BREAKPOINT;
  }
}
