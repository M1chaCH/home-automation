import {Component, Input} from '@angular/core';
import {AlarmDTO} from "../../dtos/AlarmDTO";
import {AlarmService} from "../../services/alarm.service";
import {DataUpdateDistributorService} from "../../services/data-update-distributor.service";
import {SimpleSceneDTO} from "../../dtos/scene/SceneDTO";
import {ScenesService} from "../../services/scenes.service";

@Component({
  selector: 'app-add-alarm',
  templateUrl: './add-alarm.component.html',
  styleUrls: ['./add-alarm.component.scss']
})
export class AddAlarmComponent {
  @Input() scenes: SimpleSceneDTO[] = [];

  open: boolean = false;
  alarmToCreate: AlarmDTO = {
    time: "",
    active: true,
    days: [],
    sceneId: 0,
    sceneName: ""
  };

  constructor(
    public service: AlarmService,
    private sceneService: ScenesService,
    private dataDistributor: DataUpdateDistributorService
  ) { }

  applyScene(scene: SimpleSceneDTO) {
    this.alarmToCreate.sceneId = scene.id;
    this.alarmToCreate.sceneName = scene.name;
  }

  createAlarm() {
    if(this.isValid()) {
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
      sceneId: 0,
      sceneName: ""
    };
  }

  isValid(): boolean {
    return !!this.alarmToCreate.time && this.alarmToCreate.days.length > 0 && !!this.alarmToCreate.sceneId;
  }
}
