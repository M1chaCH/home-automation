import { Component } from '@angular/core';
import {SpotifyService} from "../../../services/spotify.service";
import {Observable} from "rxjs";
import {SpotifyResourceDTO} from "../../../dtos/SpotifyResourceDTO";

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
}
