import {Component, EventEmitter, Input, Output} from '@angular/core';
import {AlarmDTO} from "../../dtos/AlarmDTO";
import {AlarmService, WeekDayIndex} from "../../services/alarm.service";
import {SpotifyResourceDTO} from "../../dtos/spotify/SpotifyResourceDTO";

@Component({
  selector: 'app-alarms-mobile',
  templateUrl: './alarms-mobile.component.html',
  styleUrls: ['./alarms-mobile.component.scss']
})
export class AlarmsMobileComponent {
  @Input() alarms: AlarmDTO[] = [];
  @Output() deleteAlarmRequest: EventEmitter<number> = new EventEmitter<number>();
  @Output() updateAlarmRequest: EventEmitter<AlarmDTO> = new EventEmitter<AlarmDTO>();
  @Output() autoSaveAlarmRequest: EventEmitter<AlarmDTO> = new EventEmitter<AlarmDTO>();

  deleting: boolean = false;
  alarmToDelete: AlarmDTO | undefined;

  alarmTimeEditing: number = -1;
  editedTime: string = "";

  constructor(
      public service: AlarmService,
  ) { }

  markAlarmForDeletion(alarm: AlarmDTO) {
    this.alarmToDelete = alarm;
    this.deleting = true;
  }

  markAlarmForEditing(alarm: AlarmDTO) {
    this.editedTime = alarm.time;
    this.alarmTimeEditing = alarm.id || -1;
  }

  deleteAlarmIfApproved(approved: boolean): void {
    if(approved)
      this.deleteAlarmRequest.emit(this.alarmToDelete?.id || -1);
  }

  changeTimeOfAlarm(alarm: AlarmDTO) {
    this.alarmTimeEditing = -1;
    alarm.time = this.editedTime;
    this.updateAlarmRequest.emit(alarm);
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
