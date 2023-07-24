import { Component } from '@angular/core';
import {tabSwitcherAnimation} from "../../animations";
import {FormControl} from "@angular/forms";
import {ScenesService} from "../../services/scenes.service";
import {DeviceDTO} from "../../dtos/DeviceDTO";
import {firstValueFrom, Observable} from "rxjs";
import {DevicesService} from "../../services/devices.service";
import {LightConfigDTO} from "../../dtos/scene/LightConfigDTO";
import {SceneLightConfigDTO} from "../../dtos/scene/SceneLightConfigDTO";
import {DataUpdateDistributorService} from "../../services/data-update-distributor.service";
import {MessageDistributorService} from "../../services/message-distributor.service";
import {ChangeSceneDTO} from "../../dtos/scene/ChangeSceneDTO";
import {SpotifyResourceDTO} from "../../dtos/spotify/SpotifyResourceDTO";

@Component({
  selector: 'app-add-scene',
  templateUrl: './add-scene.component.html',
  styleUrls: ['./add-scene.component.scss'],
  animations: [ tabSwitcherAnimation ]
})
export class AddSceneComponent {
  active: boolean = false;
  scene: ChangeSceneDTO = this.createEmptyScene();

  readonly addStages: string[] = [ "SCENE_NAME", "DEVICES", "DEVICES_X_CONFIGS", "SPOTIFY", "OVERVIEW" ];
  currentAddStageIndex: number = 0;

  sceneNameControl: FormControl = new FormControl("");
  selectedResource: SpotifyResourceDTO | undefined;

  devices$: Observable<DeviceDTO[]>;
  lightConfigs$: Observable<LightConfigDTO[]>;
  defaultLightConfig: LightConfigDTO | undefined;

  constructor(
    private service: ScenesService,
    private devicesService: DevicesService,
    private dataUpdater: DataUpdateDistributorService,
    private messageDistributor: MessageDistributorService,
  ) {
    this.devices$ = devicesService.loadAllDevices();
    this.lightConfigs$ = service.loadLightConfigs();
    this.lightConfigs$.subscribe((configs: LightConfigDTO[]) => {
      this.defaultLightConfig = configs.find(config => config.name === "_default");
    });

    this.sceneNameControl.valueChanges.subscribe(name => this.scene.name = name);
  }

  checkboxChanged(e: Event, device: DeviceDTO) {
    // @ts-ignore
    const checked: boolean = e.target!.checked;
    if(checked)
      this.scene.lights.push({
        device,
        lightConfig: this.defaultLightConfig!
      });
    else {
      const index: number = this.scene.lights.indexOf(
        this.scene.lights.find(config => config.device.name === device.name)!
      );
      this.scene.lights.splice(index, 1);
    }
  }

  async selectedDeviceConfigChange(e: Event, light: SceneLightConfigDTO) {
    // @ts-ignore
    // noinspection UnnecessaryLocalVariableJS
    const selectedLightConfig: LightConfigDTO = (await firstValueFrom(this.lightConfigs$)).find(config => config.name === e.target!.value)!;
    this.scene.lights.find(l => l.device.name === light.device.name)!.lightConfig = selectedLightConfig;
  }

  selectedResourceChanged(change: SpotifyResourceDTO | undefined): void {
    if(change)
      this.scene.spotifyResource = change.spotifyURI;
    else
      this.scene.spotifyResource = undefined;
  }

  isDeviceSelected(device: DeviceDTO): boolean {
    return this.scene.lights.find(config => config.device.name === device.name) !== undefined;
  }

  create() {
    if(this.isSceneValid())
      this.service.createScene(this.scene).subscribe(scene => {
        this.dataUpdater.updateTopic("NEW_SCENE", scene);
        this.messageDistributor.pushMessage("INFO", "created new scene " + scene.name);

        this.resetValues();
      });
  }

  resetValues() {
    this.scene = this.createEmptyScene();
    this.sceneNameControl.setValue("");
    this.active = false;
    this.selectedResource = undefined;
    this.currentAddStageIndex = 0;
  }

  isSceneValid(): boolean {
    return this.scene.name.length >= 1 && this.scene.lights.length >= 1;
  }

  private createEmptyScene(): ChangeSceneDTO {
    return {
      id: -1,
      name: "",
      defaultScene: false,
      spotifyResource: undefined,
      spotifyVolume: 30,
      lights: [],
    };
  }
}
