import {Component, EventEmitter, Input, Output} from '@angular/core';
import {SpotifyResourceDTO} from "../../dtos/spotify/SpotifyResourceDTO";

@Component({
  selector: 'app-scene-audio',
  templateUrl: './scene-audio.component.html',
  styleUrls: ['./scene-audio.component.scss']
})
export class SceneAudioComponent {
  @Input() activeResource: SpotifyResourceDTO | undefined;
  @Input() activeVolume: number = 0;
  @Input() small: boolean = false;

  @Output() activeResourceChange: EventEmitter<SpotifyResourceDTO> = new EventEmitter<SpotifyResourceDTO>();
  @Output() activeVolumeChange: EventEmitter<number> = new EventEmitter<number>();
}
