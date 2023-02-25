import {Injectable} from "@angular/core";
import {ApiService} from "./api.service";
import {apiEndpoints} from "../configuration/app.config";

@Injectable({
  providedIn: 'root'
})
export class RoomAutomationService {

  constructor(
    private api: ApiService,
  ) { }

  toggleRoom(): void {
    this.api.callApi(apiEndpoints.AUTOMATION, "PUT", {}).subscribe();
  }
}