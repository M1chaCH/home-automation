import { Component } from '@angular/core';
import {FormControl} from "@angular/forms";
import {MessageDistributorService} from "../../services/message-distributor.service";
import {ScenesService} from "../../services/scenes.service";
import {LightConfigDTO} from "../../dtos/scene/LightConfigDTO";
import {Rgba} from "@iplab/ngx-color-picker/lib/helpers/rgba.class";
import {Color} from "@iplab/ngx-color-picker";
import {DataUpdateDistributorService} from "../../services/data-update-distributor.service";

@Component({
  selector: 'app-add-light-config',
  templateUrl: './add-light-config.component.html',
  styleUrls: ['./add-light-config.component.scss']
})
export class AddLightConfigComponent {
  active: boolean = false;
  lightConfigNameControl: FormControl = new FormControl("");

  private selectedRgba: Rgba | undefined;

  constructor(
    private messageDistributor: MessageDistributorService,
    private service: ScenesService,
    private dataDistributor: DataUpdateDistributorService,
  ) { }

  create() {
    const newName: string = this.lightConfigNameControl.value;
    if(!newName)
      this.messageDistributor.pushMessage("ERROR", "Can't create, name not found");
    else if(!this.selectedRgba)
      this.messageDistributor.pushMessage("ERROR", "Can't create, color not found");
    else {
      const toCreate: LightConfigDTO = {
        id: -1,
        name: newName,
        red: this.selectedRgba.getRed(),
        green: this.selectedRgba.getGreen(),
        blue: this.selectedRgba.getBlue(),
        brightness: this.selectedRgba.getAlpha() * 100,
      };
      this.service.createLightConfig(toCreate).subscribe((created: LightConfigDTO) => {
        this.dataDistributor.updateTopic("NEW_LIGHT_CONFIG", created);
        this.active = false;
        this.selectedRgba = undefined;
        this.lightConfigNameControl.setValue("");
      });
    }
  }

  cancel() {
    this.active = false;
  }

  updateSelectedColor(color: Color) {
    this.selectedRgba = color.getRgba();
  }
}
