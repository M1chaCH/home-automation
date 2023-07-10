import {Component, Input} from '@angular/core';
import {AlarmDTO} from "../../dtos/AlarmDTO";
import {AlarmService} from "../../services/alarm.service";

@Component({
  selector: 'app-alarms-large',
  templateUrl: './alarms-large.component.html',
  styleUrls: ['./alarms-large.component.scss']
})
export class AlarmsLargeComponent {
  @Input() alarms: AlarmDTO[] = [];

  aboutToDelete: number = -1;
  editing: number = -1;
  editedTime: string = "";

  protected readonly AlarmService = AlarmService;

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
