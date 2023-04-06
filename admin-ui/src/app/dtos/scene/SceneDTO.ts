import {SceneLightConfigDTO} from "./SceneLightConfigDTO";

export type SceneDTO = {
  id: number,
  name: string,
  defaultScene: boolean,
  spotifyResource: string,
  spotifyVolume: number,
  lights: SceneLightConfigDTO[],
};