import {WeekDayIndex} from "../services/alarm.service";

export type AlarmDTO = {
  id?: number,
  days: WeekDayIndex[],
  time: string,
  active: boolean,
  sceneId: number,
  sceneName: string,
}