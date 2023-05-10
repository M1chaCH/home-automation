import {SceneLightConfigDTO} from "./SceneLightConfigDTO";

export type ChangeSceneDTO = {
  id: number,
  name: string,
  defaultScene: boolean,
  spotifyResource: string,
  spotifyVolume: number,
  lights: SceneLightConfigDTO[],
};