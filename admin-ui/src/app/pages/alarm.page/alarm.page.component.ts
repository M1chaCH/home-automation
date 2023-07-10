import { Component } from '@angular/core';
import {AlarmService} from "../../services/alarm.service";

@Component({
  selector: 'app-alarm.page',
  templateUrl: './alarm.page.component.html',
  styleUrls: ['./alarm.page.component.scss']
})
export class AlarmPageComponent {

  constructor(
    public alarmService: AlarmService,
  ) { }
}
