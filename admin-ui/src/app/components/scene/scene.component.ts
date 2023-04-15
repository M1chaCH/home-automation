import {Component, Input} from '@angular/core';
import {SceneDTO} from "../../dtos/scene/SceneDTO";
import {Subject} from "rxjs";
import {DataUpdateDistributorService} from "../../services/data-update-distributor.service";
import {ScenesService} from "../../services/scenes.service";
import {MessageDistributorService} from "../../services/message-distributor.service";
import {LightConfigDTO} from "../../dtos/scene/LightConfigDTO";

@Component({
  selector: 'app-scene',
  templateUrl: './scene.component.html',
  styleUrls: ['./scene.component.scss']
})
export class SceneComponent {
  @Input() allLightConfigs: LightConfigDTO[] | null = null;
  @Input() scene!: SceneDTO;
  openLightConfigEditorSubject: Subject<number> = new Subject<number>();
  private openEditorIndex: number = -1;

  edited: boolean = false;
  deleting: boolean = false;

  constructor(
    private service: ScenesService,
    private dataUpdater: DataUpdateDistributorService,
    private messageDistributor: MessageDistributorService,
  ) { }

  applyScene() {
    this.service.applyScene(this.scene.id);
  }

  toggleOpen(index: number) {
    let indexToSend: number = this.openEditorIndex !== index ? index : -1;

    this.openLightConfigEditorSubject.next(indexToSend);
    this.openEditorIndex = indexToSend;
  }

  renameScene(newName: string): void {
    this.scene.name = newName.toUpperCase();
    this.service.updateScene(this.scene).subscribe(() => {
      this.dataUpdater.updateTopic("UPDATED_SCENE", this.scene);
      this.messageDistributor.pushMessage("INFO", "Scene renamed.");
    });
  }

  configChanged(lightIndex: number, newConfig: string): void {
    if(lightIndex === this.openEditorIndex)
      this.toggleOpen(lightIndex);

    const config: LightConfigDTO | undefined = this.allLightConfigs?.find(c => c.name === newConfig);
    this.scene.lights[lightIndex].lightConfig = config!;
    this.edited = true;
  }

  saveChangedConfigs() {
    this.service.updateScene(this.scene).subscribe(() => {
      this.dataUpdater.updateTopic("UPDATED_SCENE", this.scene);
      this.messageDistributor.pushMessage("INFO", "Scene changes saved.");
    });
  }

  requestDelete(): void {
    this.deleting = true;
  }

  deleteSceneIfApproved(approved: boolean): void {
    if(approved) {
      this.service.deleteScene(this.scene.id).subscribe(() => {
        this.dataUpdater.updateTopic("REMOVED_SCENE", this.scene);
        this.messageDistributor.pushMessage("INFO", `Deleted scene '${this.scene.name}.'`)
      });
    }
  }
}
