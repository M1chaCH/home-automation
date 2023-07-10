
export type AlarmDTO = {
  id: number,
  cronSchedule: string,
  active: boolean,
  sceneName: string,
  audio: {
    name: string,
    imageUrl: string,
    volume: number
  },
}