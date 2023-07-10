import {Component, Input} from '@angular/core';
import {AlarmDTO} from "../../dtos/AlarmDTO";
import {WeekDayIndex} from "../../services/alarm.service";

@Component({
  selector: 'app-alarms-large',
  templateUrl: './alarms-large.component.html',
  styleUrls: ['./alarms-large.component.scss']
})
export class AlarmsLargeComponent {
  readonly DAYS: {id: WeekDayIndex, name: string}[] = [
    {id: 1, name: "monday"},
    {id: 2, name: "tuesday"},
    {id: 3, name: "wednesday"},
    {id: 4, name: "thursday"},
    {id: 5, name: "friday"},
    {id: 6, name: "saturday"},
    {id: 0, name: "sunday"},
  ];

  @Input() alarms: AlarmDTO[] = [];

  aboutToDelete: number = -1;
  editing: number = -1;
  editedTime: string = "";

  computeAlarmBadgeColor(alarm: AlarmDTO, day: WeekDayIndex): string {
    if(alarm.days.includes(day)) {
      if(alarm.active)
        return "color-mix(in srgb,var(--lemon-meringue) 40%, transparent)";
      else
        return "color-mix(in srgb,var(--lemon-meringue) 15%, transparent)";
    } else {
      return "transparent";
    }
  }

  toggleScheduleDay(alarm: AlarmDTO, day: WeekDayIndex): void {
    if(alarm.days.includes(day))
      alarm.days.splice(alarm.days.indexOf(day), 1);
    else
      alarm.days.push(day);
  }

  openTimeEditor(id: number, currentTime: string) {
    this.editedTime = currentTime;
    this.editing = id;
  }

  editAlarmTime(alarm: AlarmDTO): void {
    this.editing = -1;
    console.warn("alarm edit not implemented yet")
  }

  deleteAlarm(alarm: AlarmDTO): void {
    this.aboutToDelete = -1;
    console.warn("alarm delete not implemented yet")
  }
}
