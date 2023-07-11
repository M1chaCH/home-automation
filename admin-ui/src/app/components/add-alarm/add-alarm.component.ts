import { Component } from '@angular/core';
import {AlarmDTO} from "../../dtos/AlarmDTO";
import {AlarmService} from "../../services/alarm.service";

@Component({
  selector: 'app-add-alarm',
  templateUrl: './add-alarm.component.html',
  styleUrls: ['./add-alarm.component.scss']
})
export class AddAlarmComponent {
  open: boolean = false;
  alarmToCreate: AlarmDTO = {
    time: "",
    active: true,
    days: [],
    maxVolume: 30,
  };

  protected readonly AlarmService = AlarmService;

  createAlarm() {
    if(this.isValid()) {
      console.warn("create alarm is not yet implemented")
    }
  }

  cancel() {
    this.open = false;
    this.alarmToCreate = {
      time: "",
      active: true,
      days: [],
      maxVolume: 30,
    };
  }

  isValid(): boolean {
    return !!this.alarmToCreate.time && this.alarmToCreate.days.length > 0 && !!this.alarmToCreate.spotifyResource;
  }
}
