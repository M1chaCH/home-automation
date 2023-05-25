import {ErrorMessageDTO} from "./ErrorMessageDTO";

export type ToggleRoomResponseDTO = {
  on: boolean;
  success: boolean;
}

export type ApplyResponseDTO = {
  name: string;
  failed: boolean;
  failCause?: ErrorMessageDTO;
}

export type SceneApplyResponseDTO = {
  name: string;
  responses: ApplyResponseDTO[];
  failed: boolean;
}