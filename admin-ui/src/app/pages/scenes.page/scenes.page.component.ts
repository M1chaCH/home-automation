import { Component } from '@angular/core';
import {ScenesService} from "../../services/scenes.service";
import {SceneDTO} from "../../dtos/scene/SceneDTO";
import {
  DataTopic,
  DataUpdateDistributorService,
  DataUpdateListener
} from "../../services/data-update-distributor.service";
import {LightConfigDTO} from "../../dtos/scene/LightConfigDTO";
import {Observable} from "rxjs";

@Component({
  selector: 'app-scenes.page',
  templateUrl: './scenes.page.component.html',
  styleUrls: ['./scenes.page.component.scss']
})
export class ScenesPageComponent implements DataUpdateListener{

  scenes: SceneDTO[] | undefined;
  lightConfigs$: Observable<LightConfigDTO[]>;

  constructor(
    private service: ScenesService,
    private dataUpdater: DataUpdateDistributorService,
  ) {
    service.loadScenes().subscribe(scenes => this.scenes = scenes);
    dataUpdater.registerListener(this, "NEW_SCENE", "REMOVED_SCENE");
    this.lightConfigs$ = service.loadLightConfigs();
  }

  updateData(topic: DataTopic, data: any): void {
    switch (topic) {
      case "NEW_SCENE":
        this.scenes?.push(data);
        break;
      case "REMOVED_SCENE":
        this.scenes?.splice(this.scenes?.indexOf(data), 1);
        break;
    }
  }
}
