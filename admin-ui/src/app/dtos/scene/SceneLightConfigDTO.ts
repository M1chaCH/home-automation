import {LightConfigDTO} from "./LightConfigDTO";
import {DeviceDTO} from "../DeviceDTO";

export type SceneLightConfigDTO = {
  device: DeviceDTO,
  lightConfig: LightConfigDTO,
};