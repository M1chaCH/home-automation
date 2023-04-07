import {Component} from '@angular/core';
import {ScenesService} from "../../services/scenes.service";
import {LightConfigDTO} from "../../dtos/scene/LightConfigDTO";
import {Subject} from "rxjs";
import {
  DataTopic,
  DataUpdateDistributorService,
  DataUpdateListener
} from "../../services/data-update-distributor.service";

@Component({
  selector: 'app-light-configs.page',
  templateUrl: './light-configs.page.component.html',
  styleUrls: ['./light-configs.page.component.scss']
})
export class LightConfigsPageComponent implements DataUpdateListener{

  lightConfigs: LightConfigDTO[] | undefined; // todo add generic loading thing using a directive or so
  openLightConfigEditorSubject: Subject<number> = new Subject<number>();
  private openEditorIndex: number = -1;

  constructor(
    private service: ScenesService,
    private dataUpdater: DataUpdateDistributorService,
  ) {
    dataUpdater.registerListener(this, "NEW_LIGHT_CONFIG", "UPDATED_LIGHT_CONFIG", "REMOVED_LIGHT_CONFIG");
    service.loadLightConfigs().subscribe(lightConfigs => this.lightConfigs = lightConfigs);
  }

  toggleOpen(index: number) {
    let indexToSend: number = this.openEditorIndex !== index ? index : -1;

    this.openLightConfigEditorSubject.next(indexToSend);
    this.openEditorIndex = indexToSend;
  }

  updateData(topic: DataTopic, data: any) {
    switch (topic) {
      case "NEW_LIGHT_CONFIG":
        this.lightConfigs?.push(data);
        break;
      case "UPDATED_LIGHT_CONFIG":
        this.updateLightConfig(data);
        break;
      case "REMOVED_LIGHT_CONFIG":
        this.removeLightConfig(data);
        break;
    }
  }

  private updateLightConfig(updatedConfig: LightConfigDTO): void {
    let changedIndex: number | undefined;
    for(let i = 0; i < (this.lightConfigs?.length || 0); i++){
      if(this.lightConfigs![i].id === updatedConfig.id) {
        this.lightConfigs![i] = updatedConfig;
        changedIndex = i;
        break;
      }
    }

    if(changedIndex) this.toggleOpen(changedIndex); // closes automatically, this is used to let everybody know that this did actually close
    this.lightConfigs = [...(this.lightConfigs || [])];
  }

  private removeLightConfig(deletedConfig: LightConfigDTO): void {
    for(let i = 0; i < (this.lightConfigs?.length || 0); i++){
      if(this.lightConfigs![i].id === deletedConfig.id) {
        this.lightConfigs!.splice(i, 1);
        this.toggleOpen(-1); // closes automatically, this is used to let everybody know that this did actually close
        break;
      }
    }
  }
}
