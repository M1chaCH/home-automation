import {LightConfigDTO} from "./scene/LightConfigDTO";

export type DeviceDTO = {
  name: string;
  ip: string;
  online: boolean;
  state?: LightConfigDTO;
}