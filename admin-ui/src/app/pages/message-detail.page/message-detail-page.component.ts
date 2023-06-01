import {Component} from '@angular/core';
import {ActiveMessage, MessageDistributorService} from "../../services/message-distributor.service";
import {Router} from "@angular/router";

export type MessageData = {
  type: "ERROR" | "SCENE" | "",
  details: any,
};

@Component({
  selector: 'app-message-detail.page',
  templateUrl: './message-detail-page.component.html',
  styleUrls: ['./message-detail-page.component.scss']
})
export class MessageDetailPageComponent{
  activeMessage: ActiveMessage | undefined;

  constructor(
    public messageService: MessageDistributorService,
    private router: Router,
  ) {
    const state: any = router.getCurrentNavigation()?.extras?.state as { activeMessage: ActiveMessage };
    this.activeMessage = state?.activeMessage;
  }
}
