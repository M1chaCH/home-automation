import { Injectable } from '@angular/core';
import {ApiService} from "./api.service";
import {Observable} from "rxjs";
import {SceneDTO} from "../dtos/scene/SceneDTO";
import {apiEndpoints} from "../configuration/app.config";

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
}
