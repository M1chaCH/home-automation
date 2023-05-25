import {SceneLightConfigDTO} from "./SceneLightConfigDTO";

export type ChangeSceneDTO = {
  id: number,
  name: string,
  defaultScene: boolean,
  spotifyResource: string | undefined,
  spotifyVolume: number,
  lights: SceneLightConfigDTO[],
};