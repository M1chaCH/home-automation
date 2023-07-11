import {SpotifyResourceDTO} from "./spotify/SpotifyResourceDTO";
import {WeekDayIndex} from "../services/alarm.service";

export type AlarmDTO = {
  id?: number,
  days: WeekDayIndex[],
  time: string,
  active: boolean,
  spotifyResource?: SpotifyResourceDTO,
  maxVolume: number,
}