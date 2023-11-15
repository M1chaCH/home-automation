import { Component } from '@angular/core';
import {SpotifyService} from "../../../services/spotify.service";
import {Observable} from "rxjs";
import {SpotifyResourceDTO} from "../../../dtos/spotify/SpotifyResourceDTO";

@Component({
  selector: 'app-spotify-resources',
  templateUrl: './spotify.resources.component.html',
  styleUrls: ['./spotify.resources.component.scss']
})
export class SpotifyResourcesComponent {
  resources$: Observable<SpotifyResourceDTO[]>;

  constructor(
    private service: SpotifyService,
  ) {
    this.resources$ = this.service.fetchResources();
  }

  playPlaylist(resource: SpotifyResourceDTO, event: MouseEvent) {
    event.preventDefault();
    this.service.startContext(resource.spotifyURI).subscribe();
  }
}
