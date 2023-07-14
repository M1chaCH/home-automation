import {Component, HostListener} from '@angular/core';
import {AlarmService} from "../../services/alarm.service";
import {AlarmDTO} from "../../dtos/AlarmDTO";
import {
  DataTopic,
  DataUpdateDistributorService, DataUpdateListener
} from "../../services/data-update-distributor.service";
import {MessageDistributorService} from "../../services/message-distributor.service";

@Component({
  selector: 'app-alarm.page',
  templateUrl: './alarm.page.component.html',
  styleUrls: ['./alarm.page.component.scss']
})
export class AlarmPageComponent implements DataUpdateListener{
  alarms: AlarmDTO[] = [];
  mobileView: boolean = true;
  readonly AUTO_SAVE_WAIT: number = 5 * 1000;

  private readonly MOBILE_BREAKPOINT = 900;
  private alarmsToAutoSave: Map<number, AlarmDTO> = new Map<number, AlarmDTO>();
  private autoSaveTimer: number = -1;

  constructor(
    private service: AlarmService,
    private dataDistributor: DataUpdateDistributorService,
    private messageDistributor: MessageDistributorService,
  ) {
    dataDistributor.registerListener(this, "NEW_ALARM", "UPDATED_ALARM", "REMOVED_ALARM");
    this.onWindowResize();
    service.loadAlarms().subscribe(response => this.alarms = response);
  }

  updateData(topic: DataTopic, data: any): void {
    switch (topic) {
      case "NEW_ALARM":
        this.alarms = this.service.handleAddAlarm(data);
        break;
      case "UPDATED_ALARM":
        this.alarms = this.service.handleChangeAlarm(data);
        break;
      case "REMOVED_ALARM":
        this.alarms = this.service.handleRemoveAlarm(data);
        break;
    }
  }

  registerAlarmForAutoUpdate(alarm: AlarmDTO): void {
    clearTimeout(this.autoSaveTimer);
    this.alarmsToAutoSave.set(alarm.id!, alarm);
    this.autoSaveTimer = setTimeout(() => this.updateAlarms(this.alarmsToAutoSave), this.AUTO_SAVE_WAIT);
  }

  updateAlarm(toUpdate: AlarmDTO) {
    this.service.editAlarm(toUpdate).subscribe(() =>
      this.dataDistributor.updateTopic("UPDATED_ALARM", toUpdate)
    );
  }

  deleteAlarm(id: number): void {
    this.service.deleteAlarm(id).subscribe(() => {
      this.messageDistributor.pushMessage("INFO", "deleted alarm");
      this.dataDistributor.updateTopic("REMOVED_ALARM", id);
    });
  }

  @HostListener("window:resize")
  onWindowResize(): void {
    this.mobileView = window.innerWidth <= this.MOBILE_BREAKPOINT;
  }

  private updateAlarms(toUpdate: Map<number, AlarmDTO>): void {
    for (let toUpdateElement of toUpdate.entries()) {
      this.service.editAlarm(toUpdateElement[1]).subscribe(() => {
        this.dataDistributor.updateTopic("UPDATED_ALARM", toUpdate);
      });
    }
    this.messageDistributor.pushMessage("INFO", "saved alarm changes");
  }
}
