import { Component } from '@angular/core';
import {SpotifyService} from "../../../services/spotify.service";
import {SpotifyPlayerDTO} from "../../../dtos/spotify/SpotifyPlayerDTO";

@Component({
  selector: 'app-spotify-player',
  templateUrl: './spotify.player.component.html',
  styleUrls: ['./spotify.player.component.scss']
})
export class SpotifyPlayerComponent {
  player: SpotifyPlayerDTO | undefined;

  constructor(
    private service: SpotifyService
  ) {
    this.service.loadPlayer().subscribe(p => this.player = p);
   }

  togglePlay() {
    this.service.togglePlay().subscribe(() => this.player!.playing = !this.player!.playing);
  }

  next() {
    this.service.nextSong().subscribe(p => this.player = p);
  }

  previous() {
    this.service.previousSong().subscribe(p => this.player = p);
  }
}
