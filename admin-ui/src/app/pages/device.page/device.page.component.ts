import {Component, OnInit} from '@angular/core';
import {DeviceDTO} from "../../dtos/DeviceDTO";
import {MessageDistributorService} from "../../services/message-distributor.service";
import {DevicesService} from "../../services/devices.service";
import {
  DataTopic,
  DataUpdateDistributorService,
  DataUpdateListener
} from "../../services/data-update-distributor.service";

@Component({
  selector: 'app-device.page',
  templateUrl: './device.page.component.html',
  styleUrls: ['./device.page.component.scss']
})
export class DevicePageComponent implements OnInit, DataUpdateListener{
  devices: DeviceDTO[] = [];
  nameToAdd: string = "";
  ipToAdd: string = "";
  showAdd: boolean = false;
  showRemove: boolean = false;

  constructor(
    private service: DevicesService,
    private messageDistributor: MessageDistributorService,
    private dataUpdater: DataUpdateDistributorService,
  ) { }

  ngOnInit() {
    this.dataUpdater.registerListener(this, "DEVICE_CONFIG_CHANGE");
    this.service.loadAllDevices(true).subscribe(devices => this.devices = devices);
  }

  updateData(topic: DataTopic, data: any): void {
    if(topic === "DEVICE_CONFIG_CHANGE") {
      const changedDevice: DeviceDTO = this.devices.find(d => d.name === data.name!)!;
      changedDevice.state = data?.state;
    }
  }

  closeAdd() {
    this.nameToAdd = "";
    this.ipToAdd = "";
    this.showAdd = false;
  }

  toggleDevicePower(name: string): void {
    this.service.toggleDevicePower(name).subscribe(config =>
      this.dataUpdater.updateTopic("DEVICE_CONFIG_CHANGE", { name, state: config }));
  }

  addNew() {
    this.service.addDevice(this.nameToAdd, this.ipToAdd).subscribe(addedDevice => {
      this.devices.push(addedDevice);
      this.closeAdd();
    });
  }

  renameDevice(newName: string, device: DeviceDTO) {
    this.service.renameDevice(device.name, newName).subscribe(() => {
      device.name = newName;
      this.messageDistributor.pushMessage("INFO", "successfully renamed device");
    });
  }

  removeDevice(device: DeviceDTO) {
    this.service.removeDevice(device.name).subscribe(() => {
      this.messageDistributor.pushMessage("INFO", "successfully deleted device");

      const index: number = this.devices.indexOf(device);
      this.devices.splice(index, 1);
    });
  }
}
