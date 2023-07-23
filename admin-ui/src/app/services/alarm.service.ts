import { Injectable } from '@angular/core';
import {AlarmDTO, AlarmNotificationDTO} from "../dtos/AlarmDTO";
import {Observable, of} from "rxjs";
import {ApiService} from "./api.service";
import {apiEndpoints, websockets} from "../configuration/app.config";
import {WebSocketSubject} from "rxjs/internal/observable/dom/WebSocketSubject";
import {webSocket} from "rxjs/webSocket";
import {environment} from "../../environments/environment";

export type WeekDayIndex = 0 | 1 | 2 | 3 | 4 | 5 | 6;

@Injectable({
  providedIn: 'root'
})
export class AlarmService {
  public readonly DAYS: { id: WeekDayIndex, name: string }[] = [
    {id: 1, name: "monday"},
    {id: 2, name: "tuesday"},
    {id: 3, name: "wednesday"},
    {id: 4, name: "thursday"},
    {id: 5, name: "friday"},
    {id: 6, name: "saturday"},
    {id: 0, name: "sunday"},
  ];

  private alarms?: AlarmDTO[];
  private notificationSocket: WebSocketSubject<AlarmNotificationDTO>;

  constructor(
      private api: ApiService,
  ) {
    this.notificationSocket = webSocket<AlarmNotificationDTO>(`${environment.WS_API_URL}/${websockets.ALARM_NOTIFICATIONS}`);
  }

  connectToNotifications(): Observable<AlarmNotificationDTO> {
    return this.notificationSocket.asObservable();
  }

  loadAlarms(): Observable<AlarmDTO[]> {
    if(this.alarms)
      return of(this.alarms);

    return new Observable<AlarmDTO[]>(subscriber => {
      this.api.callApi<AlarmDTO[]>(apiEndpoints.ALARMS, "GET", undefined).subscribe(response => {
        this.alarms = this.sortByTime(response);
        subscriber.next(this.alarms);
      })
    });
  }

  createAlarm(alarm: AlarmDTO): Observable<AlarmDTO> {
    return this.api.callApi<AlarmDTO>(apiEndpoints.ALARMS, "POST", alarm);
  }

  editAlarm(alarm: AlarmDTO): Observable<any> {
    return this.api.callApi(apiEndpoints.ALARMS, "PUT", alarm);
  }

  deleteAlarm(id: number): Observable<any> {
    return this.api.callApi(`${apiEndpoints.ALARMS}/${id}`, "DELETE", undefined);
  }

  loadNextAlarm(): Observable<AlarmDTO> {
    return this.api.callApi<AlarmDTO>(apiEndpoints.CURRENT_ALARM, "GET", undefined);
  }

  continueSceneOfCurrentAlarm(): Observable<any> {
    return this.api.callApi(apiEndpoints.CURRENT_ALARM, "PUT", undefined);
  }

  stopCurrentAlarm(): Observable<any> {
    return this.api.callApi(apiEndpoints.CURRENT_ALARM, "DELETE", undefined);
  }

  handleAddAlarm(toAdd: AlarmDTO): AlarmDTO[] {
    if(!this.alarms)
      this.alarms = [];
    this.alarms.push(toAdd);
    this.alarms = this.sortByTime(this.alarms);
    return this.alarms;
  }

  handleChangeAlarm(toChange: AlarmDTO): AlarmDTO[] {
    if(!this.alarms)
      this.alarms = [];

    const toChangeIndex: number = this.alarms.findIndex(alarm => alarm.id === toChange.id);
    this.alarms[toChangeIndex] = toChange;
    this.alarms = this.sortByTime(this.alarms);

    return this.alarms;
  }

  handleRemoveAlarm(id: number): AlarmDTO[] {
    if(!this.alarms)
      this.alarms = [];

    this.alarms.splice(this.alarms.findIndex(alarm => alarm.id === id), 1);
    return this.alarms;
  }

  public parseStringDay(alarm: AlarmDTO): string {
    const currentDay: number = new Date().getDay();
    const closestDay: WeekDayIndex = this.getClosestDayIndex(alarm.days);

    if(currentDay == closestDay)
      return "Today";
    else if(closestDay == currentDay + 1)
      return "Tomorrow";
    else {
      switch (closestDay) {
        case 0:
          return "Sunday";
        case 1:
          return "Monday";
        case 2:
          return "Tuesday";
        case 3:
          return "Wednesday";
        case 4:
          return "Thursday";
        case 5:
          return "Friday";
        case 6:
          return "Saturday";
      }
    }
  }

  getClosestDayIndex(days: WeekDayIndex[]): WeekDayIndex {
    if(days.length < 2)
      return days[0];

    // prepare data into a week array
    let week: (WeekDayIndex | undefined)[] = [];
    for (let i: number = 0; i < 7; i++) {
      const dayScheduled: boolean = !!days.find(d => d == i);
      week[i] = dayScheduled ? i as WeekDayIndex : undefined;
    }

    const currentDay: WeekDayIndex = new Date().getDay() as WeekDayIndex;
    // check if next alarm is in this week
    for (let i = currentDay; i < week.length; i++) {
      if(week[i]) return i;
    }

    // alarm has to be in next week
    for (let i = 0; i < currentDay; i++) {
      if(week[i]) return i as WeekDayIndex;
    }

    console.error("for some reason no next day could be found ):, returning 0, week used: ", week);
    return 0;
  }

  public computeAlarmBadgeColor(alarm: AlarmDTO, day: WeekDayIndex): string {
    if(alarm.days.includes(day)) {
      if(alarm.active)
        return "color-mix(in srgb,var(--lemon-meringue) 40%, transparent)";
      else
        return "color-mix(in srgb,var(--lemon-meringue) 15%, transparent)";
    } else {
      return "transparent";
    }
  }

  public toggleScheduleDay(alarm: AlarmDTO, day: WeekDayIndex): void {
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
