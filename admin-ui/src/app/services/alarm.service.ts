import { Injectable } from '@angular/core';
import {AlarmDTO} from "../dtos/AlarmDTO";

export type WeekDayIndex = 0 | 1 | 2 | 3 | 4 | 5 | 6;

@Injectable({
  providedIn: 'root'
})
export class AlarmService {
  static readonly DAYS: {id: WeekDayIndex, name: string}[] = [
    {id: 1, name: "monday"},
    {id: 2, name: "tuesday"},
    {id: 3, name: "wednesday"},
    {id: 4, name: "thursday"},
    {id: 5, name: "friday"},
    {id: 6, name: "saturday"},
    {id: 0, name: "sunday"},
  ];

  private _alarms: AlarmDTO[] = [];

  get alarms(): AlarmDTO[] {
    return this._alarms;
  }

  set alarms(newAlarms: AlarmDTO[]) {
    this._alarms = this.sortByTime(newAlarms);
  }

  // TODO
  // - implement auto save / generally save
  // - implement add alarm
  constructor() {
    this.alarms = [
      { // use the setter (:
        id: 3,
        time: "07:30",
        days: [ 1,2,4 ],
        active: true,
        maxVolume: 30
      },{
        id: 1,
        time: "09:00",
        days: [ 5,6,0 ],
        active: true,
        maxVolume: 30
      },{
        id: 2,
        time: "06:45",
        days: [ 3 ],
        active: false,
        maxVolume: 30
      },
    ];
  }

  public static computeAlarmBadgeColor(alarm: AlarmDTO, day: WeekDayIndex): string {
    if(alarm.days.includes(day)) {
      if(alarm.active)
        return "color-mix(in srgb,var(--lemon-meringue) 40%, transparent)";
      else
        return "color-mix(in srgb,var(--lemon-meringue) 15%, transparent)";
    } else {
      return "transparent";
    }
  }

  public static toggleScheduleDay(alarm: AlarmDTO, day: WeekDayIndex): void {
    if(alarm.days.includes(day) && alarm.days.length > 1)
      alarm.days.splice(alarm.days.indexOf(day), 1);
    else
      alarm.days.push(day);
  }

  private sortByTime(toSort: AlarmDTO[]): AlarmDTO[] {
    return toSort.sort((a: AlarmDTO, b: AlarmDTO) => {
      const [aHour, aMinute] = a.time.split(':');
      const [bHour, bMinute] = b.time.split(':');

      if (aHour !== bHour)
        return parseInt(aHour, 10) - parseInt(bHour, 10);

      return parseInt(aMinute, 10) - parseInt(bMinute, 10);
    });
  }
}
