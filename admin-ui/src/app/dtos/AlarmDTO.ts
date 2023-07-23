import {WeekDayIndex} from "../services/alarm.service";
import {ErrorMessageDTO} from "./ErrorMessageDTO";

export type AlarmDTO = {
  id?: number,
  days: WeekDayIndex[],
  time: string,
  active: boolean,
  sceneId: number,
  sceneName: string,
}

export type AlarmNotificationDTO = {
  notificationId: "alarm" | "alarm_completed" | "error",
  body: ErrorMessageDTO | AlarmDTO | undefined;
}