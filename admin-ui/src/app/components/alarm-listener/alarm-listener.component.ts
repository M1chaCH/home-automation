import {Component, OnInit} from '@angular/core';
import {AlarmService} from "../../services/alarm.service";
import {AlarmDTO} from "../../dtos/AlarmDTO";
import {MessageDistributorService} from "../../services/message-distributor.service";

@Component({
  selector: 'app-alarm-listener',
  templateUrl: './alarm-listener.component.html',
  styleUrls: ['./alarm-listener.component.scss']
})
export class AlarmListenerComponent implements OnInit{
  runningAlarm: AlarmDTO | undefined;

  constructor(
    private service: AlarmService,
    private messageService: MessageDistributorService,
  ) { }

  ngOnInit(): void {
    this.service.connectToNotifications().subscribe(notification => {
      switch (notification.notificationId) {
        case "alarm":
          this.runningAlarm = notification.body as AlarmDTO;
          this.messageService.pushMessage("INFO", `alarm for ${this.runningAlarm.time} running!`);
          break;
        case "alarm_completed":
          this.runningAlarm = undefined;
          break;
        case "error":
          this.messageService.pushMessage("ERROR", "unexpected alarm error", {
            type: "ERROR",
            details: notification.body
          });
          this.runningAlarm = undefined;
          break;
      }
    });
  }

  continueAlarmScene() {
    this.service.continueSceneOfCurrentAlarm().subscribe(() => {
      this.messageService.pushMessage("INFO", "alarm scene started");
    });
  }

  stopAlarm() {
    this.service.stopCurrentAlarm().subscribe(() => {
      this.messageService.pushMessage("INFO", "alarm stopped");
    });
  }
}
