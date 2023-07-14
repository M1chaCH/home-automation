import {Component, EventEmitter, Input, Output} from '@angular/core';
import {AlarmDTO} from "../../dtos/AlarmDTO";
import {AlarmService, WeekDayIndex} from "../../services/alarm.service";
import {SpotifyResourceDTO} from "../../dtos/spotify/SpotifyResourceDTO";

@Component({
  selector: 'app-alarms-large',
  templateUrl: './alarms-large.component.html',
  styleUrls: ['./alarms-large.component.scss']
})
export class AlarmsLargeComponent {
  @Input() alarms: AlarmDTO[] = [];
  @Output() deleteAlarmRequest: EventEmitter<number> = new EventEmitter<number>();
  @Output() updateAlarmRequest: EventEmitter<AlarmDTO> = new EventEmitter<AlarmDTO>();
  @Output() autoSaveAlarmRequest: EventEmitter<AlarmDTO> = new EventEmitter<AlarmDTO>();

  aboutToDelete: number = -1;
  editing: number = -1;
  editedTime: string = "";

  constructor(
      public service: AlarmService,
  ) { }

  openTimeEditor(id: number, currentTime: string) {
    this.editedTime = currentTime;
    this.editing = id;
  }

  editAlarmTime(alarm: AlarmDTO): void {
    this.editing = -1;
    alarm.time = this.editedTime;
    this.updateAlarmRequest.emit(alarm);
  }

  deleteAlarm(alarm: AlarmDTO): void {
    this.aboutToDelete = -1;
    this.deleteAlarmRequest.emit(alarm.id);
  }

  changeDay(alarm: AlarmDTO, day: WeekDayIndex): void {
    this.service.toggleScheduleDay(alarm, day);
    this.autoSaveAlarmRequest.emit(alarm);
  }

  changeVolume(alarm: AlarmDTO, volume: number): void {
    alarm.maxVolume = volume;
    this.autoSaveAlarmRequest.emit(alarm);
  }

  changeResource(alarm: AlarmDTO, resource: SpotifyResourceDTO): void {
    alarm.audio = resource;
    this.autoSaveAlarmRequest.emit(alarm);
  }

  changeActive(alarm: AlarmDTO, active: boolean): void {
    alarm.active = active;
    this.autoSaveAlarmRequest.emit(alarm);
  }
}
