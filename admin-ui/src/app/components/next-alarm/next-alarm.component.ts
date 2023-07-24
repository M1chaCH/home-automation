import {Component} from '@angular/core';
import {Observable} from "rxjs";
import {AlarmDTO} from "../../dtos/AlarmDTO";
import {AlarmService} from "../../services/alarm.service";
import {
  DataTopic,
  DataUpdateDistributorService,
  DataUpdateListener
} from "../../services/data-update-distributor.service";

@Component({
  selector: 'app-next-alarm',
  templateUrl: './next-alarm.component.html',
  styleUrls: ['./next-alarm.component.scss']
})
export class NextAlarmComponent implements DataUpdateListener{
  nextAlarm$: Observable<AlarmDTO | undefined>;

  constructor(
    private service: AlarmService,
    private dataUpdater: DataUpdateDistributorService,
  ) {
    this.nextAlarm$ = service.loadNextAlarm();
    dataUpdater.registerListener(this, "UPDATED_ALARM", "NEW_ALARM", "REMOVED_ALARM");
  }

  parseStringDay(alarm: AlarmDTO): string {
    return this.service.parseStringDay(alarm);
  }

  updateData(topic: DataTopic, data: any): void {
    if(topic.includes("ALARM"))
      this.nextAlarm$ = this.service.loadNextAlarm();
  }
}
