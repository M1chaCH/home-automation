import {SceneLightConfigDTO} from "./SceneLightConfigDTO";
import {SpotifyResourceDTO} from "../spotify/SpotifyResourceDTO";

export type SceneDTO = {
  id: number,
  name: string,
  defaultScene: boolean,
  spotifyResource: SpotifyResourceDTO,
  spotifyVolume: number,
  lights: SceneLightConfigDTO[],
};

export type SimpleSceneDTO = {
  id: number,
  name: string,
  defaultScene: boolean,
  spotifyResourceName: string,
  spotifyResourceImage: string,
  spotifyVolume: number,
  deviceCount: number,
};