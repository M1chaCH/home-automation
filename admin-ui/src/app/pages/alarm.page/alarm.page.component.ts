import {Component} from '@angular/core';
import {AlarmService, WeekDayIndex} from "../../services/alarm.service";
import {AlarmDTO} from "../../dtos/AlarmDTO";
import {
  DataTopic,
  DataUpdateDistributorService, DataUpdateListener
} from "../../services/data-update-distributor.service";
import {MessageDistributorService} from "../../services/message-distributor.service";
import {SimpleSceneDTO} from "../../dtos/scene/SceneDTO";
import {ScenesService} from "../../services/scenes.service";

@Component({
  selector: 'app-alarm.page',
  templateUrl: './alarm.page.component.html',
  styleUrls: ['./alarm.page.component.scss']
})
export class AlarmPageComponent implements DataUpdateListener{
  alarms: AlarmDTO[] = [];
  simpleScenes: SimpleSceneDTO[] = [];
  readonly AUTO_SAVE_WAIT: number = 5 * 1000;

  deleting: boolean = false;
  alarmToDelete: AlarmDTO | undefined;

  alarmTimeEditing: number = -1;
  editedTime: string = "";

  private alarmsToAutoSave: Map<number, AlarmDTO> = new Map<number, AlarmDTO>();
  private autoSaveTimer: number = -1;

  constructor(
    public service: AlarmService,
    private sceneService: ScenesService,
    private dataDistributor: DataUpdateDistributorService,
    private messageDistributor: MessageDistributorService,
  ) {
    dataDistributor.registerListener(this, "NEW_ALARM", "UPDATED_ALARM", "REMOVED_ALARM");
    service.loadAlarms().subscribe(response => this.alarms = response);
    this.sceneService.loadSimpleScenes().subscribe(response => this.simpleScenes = response);
  }

  updateData(topic: DataTopic, data: any): void {
    switch (topic) {
      case "NEW_ALARM":
        this.alarms = this.service.handleAddAlarm(data);
        break;
      case "UPDATED_ALARM":
        this.alarms = this.service.handleChangeAlarm(data);
        break;
      case "REMOVED_ALARM":
        this.alarms = this.service.handleRemoveAlarm(data);
        break;
    }
  }

  registerAlarmForAutoUpdate(alarm: AlarmDTO): void {
    clearTimeout(this.autoSaveTimer);
    this.alarmsToAutoSave.set(alarm.id!, alarm);
    this.autoSaveTimer = setTimeout(() => {
      this.updateAlarms(this.alarmsToAutoSave);
      this.alarmsToAutoSave.clear();
    }, this.AUTO_SAVE_WAIT);
  }

  deleteAlarm(id: number): void {
    this.service.deleteAlarm(id).subscribe(() => {
      this.messageDistributor.pushMessage("INFO", "deleted alarm");
      this.dataDistributor.updateTopic("REMOVED_ALARM", id);
    });
  }

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
      this.deleteAlarm(this.alarmToDelete!.id!);
  }

  changeTimeOfAlarm(alarm: AlarmDTO) {
    this.alarmTimeEditing = -1;
    alarm.time = this.editedTime;
    this.registerAlarmForAutoUpdate(alarm);
  }

  changeDay(alarm: AlarmDTO, day: WeekDayIndex): void {
    this.service.toggleScheduleDay(alarm, day);
    this.registerAlarmForAutoUpdate(alarm);
  }

  changeScene(alarm: AlarmDTO, scene: SimpleSceneDTO): void {
    alarm.sceneId = scene.id;
    alarm.sceneName = scene.name;
    this.registerAlarmForAutoUpdate(alarm);
  }

  changeActive(alarm: AlarmDTO, active: boolean): void {
    alarm.active = active;
    this.registerAlarmForAutoUpdate(alarm);
  }

  private updateAlarms(toUpdate: Map<number, AlarmDTO>): void {
    for (let toUpdateElement of toUpdate.entries()) {
      this.service.editAlarm(toUpdateElement[1]).subscribe(() => {
        this.dataDistributor.updateTopic("UPDATED_ALARM", toUpdate);
      });
    }
    this.messageDistributor.pushMessage("INFO", "saved alarm changes");
  }
}
