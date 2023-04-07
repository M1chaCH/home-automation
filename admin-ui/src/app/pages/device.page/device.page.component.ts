import {Component, OnInit} from '@angular/core';
import {DeviceDTO} from "../../dtos/DeviceDTO";
import {ApiService} from "../../services/api.service";
import {apiEndpoints} from "../../configuration/app.config";
import {MessageDistributorService} from "../../services/message-distributor.service";

@Component({
  selector: 'app-device.page',
  templateUrl: './device.page.component.html',
  styleUrls: ['./device.page.component.scss']
})
export class DevicePageComponent implements OnInit{
  devices: DeviceDTO[] = [];
  nameToAdd: string = "";
  ipToAdd: string = "";
  showAdd: boolean = false;
  showRemove: boolean = false;

  constructor(
    private api: ApiService,
    private messageDistributor: MessageDistributorService,
  ) { }

  ngOnInit() {
    this.api.callApi<DeviceDTO[]>(apiEndpoints.DEVICES, "GET", {}).subscribe(
      devices => this.devices = devices);
  }

  toggleDevicePower(name: string) {
    this.api.callApi(`${apiEndpoints.DEVICES}/${name}`, "PUT", {}).subscribe();
  }

  closeAdd() {
    this.nameToAdd = "";
    this.ipToAdd = "";
    this.showAdd = false;
  }

  addNew() {
    this.api.callApi<DeviceDTO>(apiEndpoints.DEVICES, "POST", {
      name: this.nameToAdd,
      ip: this.ipToAdd
    }).subscribe(addedDevice => {
      this.devices.push(addedDevice);
      this.closeAdd();
    });
  }

  renameDevice(newName: string, device: DeviceDTO) {
    this.api.callApi(apiEndpoints.DEVICES, "PUT", { oldName: device.name, newName }).subscribe(() => {
      device.name = newName;
      this.messageDistributor.pushMessage("INFO", "successfully renamed device");
    });
  }

  removeDevice(device: DeviceDTO) {
    this.api.callApi(`${apiEndpoints.DEVICES}/${device.name}`, "DELETE", {}).subscribe(() => {
      this.messageDistributor.pushMessage("INFO", "successfully deleted device");

      const index: number = this.devices.indexOf(device);
      this.devices.splice(index, 1);
    });
  }
}
