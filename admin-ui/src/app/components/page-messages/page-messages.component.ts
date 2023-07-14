import { Component, OnInit } from '@angular/core';
import {
  ActiveMessage,
  MessageChangeListener,
  MessageDistributorService
} from "../../services/message-distributor.service";
import {appRoutes} from "../../configuration/app.config";

@Component({
  selector: 'app-page-messages',
  templateUrl: './page-messages.component.html',
  styleUrls: ['./page-messages.component.scss']
})
export class PageMessagesComponent implements MessageChangeListener, OnInit {
  private readonly MESSAGE_ANIM_DURATION_MS: number = 200;
  private readonly MESSAGE_ANIM_VAR_NAME: string = "--message-animation-duration";
  private readonly HIDE_MESSAGE_CLASS: string = "hide-message";
  private readonly listenerId: number;
  private readonly minSwipePercent: number = 50;

  messages: Map<number, ActiveMessage> = new Map<number, ActiveMessage>();

  public readonly messageRoute: string = `/${appRoutes.ROOT}/${appRoutes.MESSAGE_DETAILS}`;

  constructor(
    private service: MessageDistributorService,
  ) {
    this.listenerId = service.registerListener(this);
  }

  ngOnInit(): void {
    document.querySelector<HTMLDivElement>(".messages-container")!.style.setProperty(this.MESSAGE_ANIM_VAR_NAME,
      this.MESSAGE_ANIM_DURATION_MS + "ms");
  }

  messageValues(): ActiveMessage[] {
    return [...this.messages.values()];
  }

  messageActivated(activatedMessage: ActiveMessage): void {
    this.messages.set(activatedMessage.id!, activatedMessage);
  }

  messageExpired(expiredMessage: ActiveMessage): void {
    const messageElement: HTMLElement | null = document.getElementById("message-" + expiredMessage.id)!; // might be null due to swipe removal
    messageElement?.classList.add(this.HIDE_MESSAGE_CLASS);
    setTimeout(() => this.messages.delete(expiredMessage.id || -1), this.MESSAGE_ANIM_DURATION_MS);
  }

  messageSwiped(element: HTMLElement, message: ActiveMessage): void {
    const swipePercent: number = 100 / element.clientWidth * (element.clientWidth - element.scrollLeft);
    if(swipePercent >= this.minSwipePercent)
      this.messages.delete(message.id || -1);
  }
}
