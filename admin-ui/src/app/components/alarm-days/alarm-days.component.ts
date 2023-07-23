import {Component, EventEmitter, HostListener, Input, Output} from '@angular/core';
import {AlarmService, WeekDayIndex} from "../../services/alarm.service";
import {AlarmDTO} from "../../dtos/AlarmDTO";

@Component({
  selector: 'app-alarm-days',
  templateUrl: './alarm-days.component.html',
  styleUrls: ['./alarm-days.component.scss']
})
export class AlarmDaysComponent {
  @Input() alarms: AlarmDTO[] | undefined;
  @Input() height: string = "100%";
  @Input() width: string = "100%";

  @Output() dayClicked: EventEmitter<{ alarm: AlarmDTO, day: WeekDayIndex }> = new EventEmitter<{ alarm: AlarmDTO, day: WeekDayIndex }>();
  @Output() disableAlarm: EventEmitter<AlarmDTO> = new EventEmitter<AlarmDTO>();

  windowWidth!: number;

  constructor(
    public service: AlarmService,
  ) {
    if(!this.alarms)
      this.service.loadAlarms()
        .subscribe(alarms => this.alarms = alarms);
  }

  @HostListener("window:resize")
  windowSizeChange(): void {
    this.windowWidth = window.innerWidth;
  }
}
