import {Component, Input} from '@angular/core';
import {AlarmDTO} from "../../dtos/AlarmDTO";
import {AlarmService} from "../../services/alarm.service";

@Component({
  selector: 'app-alarms-mobile',
  templateUrl: './alarms-mobile.component.html',
  styleUrls: ['./alarms-mobile.component.scss']
})
export class AlarmsMobileComponent {
  @Input() alarms: AlarmDTO[] = [];
  deleting: boolean = false;
  alarmToDelete: AlarmDTO | undefined;

  alarmTimeEditing: number = -1;
  editedTime: string = "";

  protected readonly AlarmService = AlarmService;

  markAlarmForDeletion(alarm: AlarmDTO) {
    this.alarmToDelete = alarm;
    this.deleting = true;
  }

  markAlarmForEditing(alarm: AlarmDTO) {
    this.editedTime = alarm.time;
    this.alarmTimeEditing = alarm.id;
  }

  deleteAlarmIfApproved(approved: boolean): void {
    if(approved) {
      console.warn("delete alarm not implemented yet")
    }
  }

  changeTimeOfAlarm(alarm: AlarmDTO) {
    console.warn("edit alarm time not implemented yet")
    this.alarmTimeEditing = -1;
  }
}
