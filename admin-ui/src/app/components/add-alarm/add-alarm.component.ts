import { Component } from '@angular/core';
import {AlarmDTO} from "../../dtos/AlarmDTO";
import {AlarmService} from "../../services/alarm.service";
import {DataUpdateDistributorService} from "../../services/data-update-distributor.service";

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

  constructor(
    public service: AlarmService,
    private dataDistributor: DataUpdateDistributorService
  ) { }

  createAlarm() {
    if(this.isValid()) {
      console.log(this.alarmToCreate)
      this.service.createAlarm(this.alarmToCreate).subscribe(createdAlarm => {
        this.dataDistributor.updateTopic("NEW_ALARM", createdAlarm);
        this.reset();
      });
    }
  }

  reset() {
    this.open = false;
    this.alarmToCreate = {
      time: "",
      active: true,
      days: [],
      maxVolume: 30,
    };
  }

  isValid(): boolean {
    return !!this.alarmToCreate.time && this.alarmToCreate.days.length > 0 && !!this.alarmToCreate.audio;
  }
}
