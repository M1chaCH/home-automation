import {Component, OnInit} from '@angular/core';
import {DeviceDTO} from "../../dtos/DeviceDTO";
import {MessageDistributorService} from "../../services/message-distributor.service";
import {DevicesService} from "../../services/devices.service";

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
    private service: DevicesService,
    private messageDistributor: MessageDistributorService,
  ) { }

  ngOnInit() {
    this.service.loadAllDevices().subscribe(devices => this.devices = devices);
  }

  closeAdd() {
    this.nameToAdd = "";
    this.ipToAdd = "";
    this.showAdd = false;
  }

  toggleDevicePower(name: string): void {
    this.service.toggleDevicePower(name);
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
