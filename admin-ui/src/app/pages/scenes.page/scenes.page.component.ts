import { Component } from '@angular/core';
import {ScenesService} from "../../services/scenes.service";
import {SceneDTO} from "../../dtos/scene/SceneDTO";
import {Observable} from "rxjs";

@Component({
  selector: 'app-scenes.page',
  templateUrl: './scenes.page.component.html',
  styleUrls: ['./scenes.page.component.scss']
})
export class ScenesPageComponent {

  scenes$: Observable<SceneDTO[]>;

  constructor(
    private service: ScenesService,
  ) {
    this.scenes$ = service.loadScenes();
  }


}
