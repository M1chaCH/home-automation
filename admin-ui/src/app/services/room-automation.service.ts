import {Injectable} from "@angular/core";
import {ApiService} from "./api.service";
import {apiEndpoints} from "../configuration/app.config";
import {Observable} from "rxjs";
import {ToggleRoomResponseDTO} from "../dtos/ApplyResponseDTOs";

@Injectable({
  providedIn: 'root'
})
export class RoomAutomationService {

  constructor(
    private api: ApiService,
  ) { }

  toggleRoom(): Observable<ToggleRoomResponseDTO> {
    return this.api.callApi<ToggleRoomResponseDTO>(apiEndpoints.AUTOMATION, "PUT", {});
  }
}