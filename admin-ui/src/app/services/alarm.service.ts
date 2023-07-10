import { Injectable } from '@angular/core';
import {AlarmDTO} from "../dtos/AlarmDTO";

export type WeekDayIndex = 0 | 1 | 2 | 3 | 4 | 5 | 6;

@Injectable({
  providedIn: 'root'
})
export class AlarmService {
  private _alarms: AlarmDTO[] = [];

  get alarms(): AlarmDTO[] {
    return this._alarms;
  }

  set alarms(newAlarms: AlarmDTO[]) {
    this._alarms = this.sortByTime(newAlarms);
  }

  constructor() {
    this.alarms = [
      { // use the setter (:
        id: 3,
        time: "07:30",
        days: [ 1,2,4 ],
        active: true,
        spotifyResource: {
          name: "Worship",
          imageUrl: "",
          description: "",
          href: "",
          spotifyURI: ""
        },
        maxVolume: 30
      },{
        id: 1,
        time: "09:00",
        days: [ 5,6,0 ],
        active: true,
        spotifyResource: {
          name: "Worship",
          imageUrl: "",
          description: "",
          href: "",
          spotifyURI: ""
        },
        maxVolume: 30
      },{
        id: 2,
        time: "06:45",
        days: [ 3 ],
        active: false,
        spotifyResource: {
          name: "Worship",
          imageUrl: "",
          description: "",
          href: "",
          spotifyURI: ""
        },
        maxVolume: 30
      },
    ];
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
