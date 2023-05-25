import {Injectable} from '@angular/core';
import {MessageData} from "../pages/message-detail.page/message-detail-page.component";

export interface MessageChangeListener {
  messageActivated(activatedMessage: ActiveMessage): void;
  messageExpired(expiredMessage: ActiveMessage): void;
}

export type ActiveMessage = {
  id?: number,
  message: string,
  type: "ERROR" | "INFO",
  data?: MessageData,
};

/**
 * This service provides a global messaging functionality.
 * It tightly works together with the "page-messages" component.
 */
@Injectable({
  providedIn: 'root'
})
export class MessageDistributorService {
  private readonly MESSAGE_DISPLAY_TIME: number = 7 * 1000;

  activeMessages: Map<number, ActiveMessage> = new Map<number, ActiveMessage>();

  private messageIdIncrementor: number = 0;
  private listenerIncrementor: number = 0;
  private listeners: Map<number, MessageChangeListener> = new  Map<number, MessageChangeListener>();

  /**
   * Use this to display a message on the screen. The "page-messages" component will listen for it
   * @param type the type of the message
   * @param message the message
   * @param data an optional parameter that can be used to add additional data to the message
   */
  pushMessage(type: "ERROR" | "INFO", message: string, data?: MessageData) {
    const activeMessage: ActiveMessage = { type, message, data };
    this.messageIdIncrementor++;
    activeMessage.id = this.messageIdIncrementor;
    this.activeMessages.set(this.messageIdIncrementor, activeMessage);
    setTimeout(() => this.expireMessage(activeMessage), this.MESSAGE_DISPLAY_TIME);

    for (let listener of this.listeners.values())
      listener.messageActivated(activeMessage);
  }

  /**
   * This method is used to make the messages disappear again.
   * @param message
   */
  private expireMessage(message: ActiveMessage) {
    this.activeMessages.delete(message.id!);

    for (let listener of this.listeners.values())
      listener.messageExpired(message);
  }

  /**
   * Use this method to register a new message listener to get notified about new messages.
   * @param listener
   * @returns
   */
  registerListener(listener: MessageChangeListener): number {
    this.listenerIncrementor++;
    this.listeners.set(this.listenerIncrementor, listener);

    for (let message of this.activeMessages.values())
      listener.messageActivated(message);
    return this.listenerIncrementor;
  }

  // noinspection JSUnusedGlobalSymbols (could be of use in future)
  unregisterListener(id: number) {
    this.listeners.delete(id);
  }
}
