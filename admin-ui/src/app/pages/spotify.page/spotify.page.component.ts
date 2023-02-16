import {Component} from '@angular/core';
import {ApiService} from "../../services/api.service";
import {catchError, Observable, of} from "rxjs";
import {SpotifyAuthorisationDTO} from "../../dtos/SpotifyAuthorisationDTO";
import {apiEndpoints} from "../../configuration/app.config";

@Component({
  selector: 'app-spotify.page',
  templateUrl: './spotify.page.component.html',
  styleUrls: ['./spotify.page.component.scss']
})
export class SpotifyPageComponent {
  spotifyAuthorisation$: Observable<SpotifyAuthorisationDTO | undefined>;

  constructor(
    private api: ApiService,
  ) {
    this.spotifyAuthorisation$ = this.api.callApi<SpotifyAuthorisationDTO>(
      apiEndpoints.SPOTIFY,
      "GET",
      undefined
    ).pipe(
      catchError(() => of(undefined)),
    );
  }

}
