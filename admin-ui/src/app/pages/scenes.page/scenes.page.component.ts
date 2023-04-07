import { Component } from '@angular/core';
import {ScenesService} from "../../services/scenes.service";
import {SceneDTO} from "../../dtos/scene/SceneDTO";

@Component({
  selector: 'app-scenes.page',
  templateUrl: './scenes.page.component.html',
  styleUrls: ['./scenes.page.component.scss']
})
export class ScenesPageComponent {

  scenes: SceneDTO[] | undefined;

  constructor(
    private service: ScenesService,
  ) {
    service.loadScenes().subscribe(scenes => this.scenes = scenes);
  }


}
