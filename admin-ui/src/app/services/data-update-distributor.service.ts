import { Injectable } from '@angular/core';

export interface DataUpdateListener {
  updateData(topic: DataTopic, data: any): void;
}

export type DataTopic  = "NEW_SCENE" | "UPDATED_SCENE" | "REMOVED_SCENE" |
                         "NEW_LIGHT_CONFIG" | "UPDATED_LIGHT_CONFIG" | "REMOVED_LIGHT_CONFIG";

@Injectable({
  providedIn: 'root'
})
export class DataUpdateDistributorService {

  private listeners: Map<DataTopic, DataUpdateListener[]> = new Map<DataTopic, DataUpdateListener[]>([
    ["NEW_SCENE", []],
    ["UPDATED_SCENE", []],
    ["REMOVED_SCENE", []],
    ["NEW_LIGHT_CONFIG", []],
    ["UPDATED_LIGHT_CONFIG", []],
    ["REMOVED_LIGHT_CONFIG", []],
  ]);

  updateTopic(topic: DataTopic, data: any): void {
    this.listeners.get(topic)?.forEach((l: DataUpdateListener) => l.updateData(topic, data));
  }

  registerListener(listener: DataUpdateListener, ...topics: DataTopic[]): void {
    for (let topic of topics)
      this.listeners.get(topic)!.push(listener);
  }
}
