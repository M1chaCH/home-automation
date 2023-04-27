import { Injectable } from '@angular/core';
import {ApiService} from "./api.service";
import {apiEndpoints} from "../configuration/app.config";
import {Observable} from "rxjs";
import {DeviceDTO} from "../dtos/DeviceDTO";
import {LightConfigDTO} from "../dtos/scene/LightConfigDTO";

@Injectable({
  providedIn: 'root'
})
export class DevicesService {

  constructor(
    private api: ApiService,
  ) { }

  loadAllDevices(withState: boolean = false): Observable<DeviceDTO[]> {
    if(withState)
      return this.api.callApi<DeviceDTO[]>(`${apiEndpoints.DEVICES}?includeState=${withState}`,
        "GET", {});

    return this.api.callApi<DeviceDTO[]>(apiEndpoints.DEVICES, "GET", {});
  }

  toggleDevicePower(name: string): Observable<LightConfigDTO | undefined> {
    return this.api.callApi<LightConfigDTO | undefined>(`${apiEndpoints.DEVICES}/${name}`, "PUT", {});
  }

  addDevice(name: string, ip: string): Observable<DeviceDTO> {
    return this.api.callApi<DeviceDTO>(apiEndpoints.DEVICES, "POST", {name, ip});
  }
  
  renameDevice(oldName: string, newName: string): Observable<void> {
    return this.api.callApi(apiEndpoints.DEVICES, "PUT", { oldName, newName });
  }

  removeDevice(deviceName: string): Observable<void> {
    return this.api.callApi(`${apiEndpoints.DEVICES}/${deviceName}`, "DELETE", {});
  }
}
