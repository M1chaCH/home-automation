import { Component, OnInit } from '@angular/core';
import {
  ActiveMessage,
  MessageChangeListener,
  MessageDistributorService
} from "../../services/message-distributor.service";

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

  messages: Map<number, ActiveMessage> = new Map<number, ActiveMessage>();

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
    const messageElement: HTMLElement = document.getElementById("message-" + expiredMessage.id)!;
    messageElement.classList.add(this.HIDE_MESSAGE_CLASS);
    setTimeout(() => this.messages.delete(expiredMessage.id || -1), this.MESSAGE_ANIM_DURATION_MS);
  }

}
