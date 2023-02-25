import { Component } from '@angular/core';
import {RoomAutomationService} from "../../services/room-automation.service";

@Component({
  selector: 'app-home.page',
  templateUrl: './home.page.component.html',
  styleUrls: ['./home.page.component.scss']
})
export class HomePageComponent {

  constructor(
    private automationService: RoomAutomationService,
  ) { }

  clicked() {
    this.automationService.toggleRoom();
  }
}
