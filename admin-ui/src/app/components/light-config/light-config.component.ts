import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LightConfigDTO} from "../../dtos/scene/LightConfigDTO";
import {Observable, of} from "rxjs";
import {Color} from "@iplab/ngx-color-picker";
import {Rgba} from "@iplab/ngx-color-picker/lib/helpers/rgba.class";
import {ScenesService} from "../../services/scenes.service";
import {DataUpdateDistributorService} from "../../services/data-update-distributor.service";
import {MessageDistributorService} from "../../services/message-distributor.service";

@Component({
  selector: 'app-light-config',
  templateUrl: './light-config.component.html',
  styleUrls: ['./light-config.component.scss']
})
export class LightConfigComponent implements OnInit{
  readonly DEFAUL_LIGHT_CONFIG_NAME = "_default";

  @Input() lightConfig!: LightConfigDTO;
  @Input() index!: number;
  editorOpen: boolean = false;

  @Input() openEditorId$: Observable<number> = of(-1);
  @Output() requestToggleOpen: EventEmitter<boolean> = new EventEmitter<boolean>();

  deleteVerifyPopupOpen: boolean = false;

  private initialLightConfig: LightConfigDTO = this.lightConfig;

  constructor(
    private service: ScenesService,
    private dataUpdater: DataUpdateDistributorService,
    private messageDistributor: MessageDistributorService,
  ) { }

  ngOnInit() {
    this.initialLightConfig = structuredClone(this.lightConfig);

    this.openEditorId$.subscribe(index => {
      this.editorOpen = !this.editorOpen;
      this.editorOpen = index === this.index && this.editorOpen;
    });
  }

  colorPickerChange(newColor: Color): void {
    const color: Rgba = newColor.getRgba();
    this.lightConfig.red = color.getRed();
    this.lightConfig.green = color.getGreen();
    this.lightConfig.blue = color.getBlue();
    this.lightConfig.brightness = color.getAlpha() * 100;
    this.lightConfig = {... this.lightConfig}; // to let the template know that stuff has changed
  }

  toggleOpen() {
    this.requestToggleOpen.next(!this.editorOpen);
  }

  rename(newName: string) {
    this.lightConfig.name = newName;
  }

  saveChanges(): void {
    this.service.updateLightConfig(this.lightConfig).subscribe(() => {
      this.dataUpdater.updateTopic("UPDATED_LIGHT_CONFIG", this.lightConfig);
      this.messageDistributor.pushMessage("INFO", "saved changes to light config");
    });
  }

  deleteIfAllowed(allowed: boolean) {
    if(allowed) {
      if(this.lightConfig.name === this.DEFAUL_LIGHT_CONFIG_NAME){
        this.messageDistributor.pushMessage("INFO", "Can't delete default config!");
        return;
      } else {
        this.service.removeLightConfig(this.lightConfig.id).subscribe(() =>
          this.dataUpdater.updateTopic("REMOVED_LIGHT_CONFIG", this.lightConfig));
      }
    }
  }

  reset(): void {
    this.lightConfig = structuredClone(this.initialLightConfig);
  }


}
