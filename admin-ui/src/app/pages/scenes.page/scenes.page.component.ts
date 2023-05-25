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
import {SpotifyService} from "../../services/spotify.service";

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
    private spotifyService: SpotifyService,
    private dataUpdater: DataUpdateDistributorService,
  ) {
    // load playlists into cache -> if not done here it might happen that every scene loads it from the api, this way
    // we can be sure that the cache is loaded
    spotifyService.fetchResources().subscribe();

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
