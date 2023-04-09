import { Injectable } from '@angular/core';
import {ApiService} from "./api.service";
import {Observable} from "rxjs";
import {SceneDTO} from "../dtos/scene/SceneDTO";
import {apiEndpoints} from "../configuration/app.config";
import {LightConfigDTO} from "../dtos/scene/LightConfigDTO";

@Injectable({
  providedIn: 'root'
})
export class ScenesService {

  constructor(
    private api: ApiService,
  ) { }

  loadScenes(): Observable<SceneDTO[]> {
    return this.api.callApi<SceneDTO[]>(apiEndpoints.SCENE_CRUD, "GET", undefined);
  }

  createScene(scene: SceneDTO): Observable<SceneDTO> {
    // todo
  }

  loadLightConfigs(): Observable<LightConfigDTO[]> {
    return this.api.callApi<LightConfigDTO[]>(apiEndpoints.CONFIG_CRUD, "GET", undefined);
  }

  createLightConfig(lightConfig: LightConfigDTO): Observable<LightConfigDTO> {
    return this.api.callApi<LightConfigDTO>(apiEndpoints.CONFIG_CRUD, "POST", lightConfig);
  }

  updateLightConfig(lightConfig: LightConfigDTO): Observable<void> {
    return this.api.callApi<void>(apiEndpoints.CONFIG_CRUD, "PUT", lightConfig);
  }

  removeLightConfig(configId: number): Observable<void> {
    return this.api.callApi<void>(`${apiEndpoints.CONFIG_CRUD}/${configId}`, "DELETE", undefined);
  }
}
