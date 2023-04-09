import { Injectable } from '@angular/core';
import {ApiService} from "./api.service";
import {apiEndpoints} from "../configuration/app.config";
import {Observable} from "rxjs";
import {DeviceDTO} from "../dtos/DeviceDTO";

@Injectable({
  providedIn: 'root'
})
export class DevicesService {

  constructor(
    private api: ApiService,
  ) { }

  loadAllDevices(): Observable<DeviceDTO[]> {
    return this.api.callApi<DeviceDTO[]>(apiEndpoints.DEVICES, "GET", {});
  }

  toggleDevicePower(name: string) {
    this.api.callApi(`${apiEndpoints.DEVICES}/${name}`, "PUT", {}).subscribe();
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
