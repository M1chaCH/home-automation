import { Component } from '@angular/core';
import {AlarmDTO} from "../../dtos/AlarmDTO";
import parser, {CronExpression, DayOfTheWeekRange} from "cron-parser";

@Component({
  selector: 'app-alarm.page',
  templateUrl: './alarm.page.component.html',
  styleUrls: ['./alarm.page.component.scss']
})
export class AlarmPageComponent {
  readonly DAYS: {id: DayOfTheWeekRange, name: string}[] = [
    {id: 1, name: "monday"},
    {id: 2, name: "tuesday"},
    {id: 3, name: "wednesday"},
    {id: 4, name: "thursday"},
    {id: 5, name: "friday"},
    {id: 6, name: "saturday"},
    {id: 0, name: "sunday"},
  ];
  alarms: AlarmDTO[] = [ {
    id: 3,
    cronSchedule: "30 7 * * 1,4,2",
    active: true,
    sceneName: "Default",
    audio: {
      name: "Worship",
      imageUrl: "",
      volume: 30,
    }
  },{
    id: 1,
    cronSchedule: "0 9 * * 5,6,0",
    active: true,
    sceneName: "Default",
    audio: {
      name: "Worship",
      imageUrl: "",
      volume: 30,
    }
  },{
    id: 2,
    cronSchedule: "45 6 * * 3",
    active: false,
    sceneName: "Default",
    audio: {
      name: "Worship",
      imageUrl: "",
      volume: 30,
    }
  },
  ];

  constructor() {
    this.alarms = this.sortByTime();
  }

  isOnDay(cronSchedule: string, day: DayOfTheWeekRange): boolean {
    const interval: CronExpression = parser.parseExpression(cronSchedule);
    return interval.fields.dayOfWeek.includes(day);
  }

  extractTime(alarm: AlarmDTO): string {
    const interval: CronExpression = parser.parseExpression(alarm.cronSchedule);
    return `${interval.fields.hour}:${interval.fields.minute}`;
  }

  computeAlarmBadgeColor(alarm: AlarmDTO, day: DayOfTheWeekRange): string {
    if(this.isOnDay(alarm.cronSchedule, day)) {
      if(alarm.active)
        return "color-mix(in srgb,var(--lemon-meringue) 40%, transparent)";
      else
        return "color-mix(in srgb,var(--lemon-meringue) 15%, transparent)";
    } else {
      return "transparent";
    }
  }

  toggleScheduleDay(alarm: AlarmDTO, day: DayOfTheWeekRange): void {
    const schedule: string = alarm.cronSchedule;
    let newSchedule: string = schedule.substring(0, schedule.lastIndexOf(" ") + 1);
    const scheduledDays: string = schedule.substring(schedule.lastIndexOf(" ") + 1);
    const toRemove: boolean = scheduledDays.includes(`${day}`);

    if(toRemove)
      newSchedule += scheduledDays.replace(`,${day}`, "").replace(`${day},`, "");
    else
      newSchedule += scheduledDays + `,${day}`;

    alarm.cronSchedule = newSchedule;
  }

  private sortByTime(): AlarmDTO[] {
    return this.alarms.sort((a: AlarmDTO, b: AlarmDTO) => {
      const [aHour, aMinute] = a.cronSchedule.split(' ')[1].split(':');
      const [bHour, bMinute] = b.cronSchedule.split(' ')[1].split(':');

      if (aHour !== bHour)
        return parseInt(aHour, 10) - parseInt(bHour, 10);

      return parseInt(aMinute, 10) - parseInt(bMinute, 10);
    });
  }
}
