import {Component, Input, OnInit} from '@angular/core';
import {appRoutes} from "../../../configuration/app.config";
import {environment} from "../../../../environments/environment";
import {SpotifyService} from "../../../services/spotify.service";
import {SpotifyClientDTO} from "../../../dtos/SpotifyClientDTO";

@Component({
  selector: 'app-spotify-authorisation',
  templateUrl: './spotify-authorisation.component.html',
  styleUrls: ['./spotify-authorisation.component.scss']
})
export class SpotifyAuthorisationComponent implements OnInit{

  @Input() state: string | undefined;
  @Input() code: string | undefined;
  @Input() error: string | undefined;
  @Input() client!: SpotifyClientDTO;

  authorisationState: "initial" | "success" | "error" = "initial";
  readonly BACK_ROUTE: string = `/${appRoutes.ROOT}/${appRoutes.HOME}`;
  readonly SPOTIFY_AUTH_URL: string = "https://accounts.spotify.com/authorize";

  constructor(
    private service: SpotifyService,
  ) { }

  ngOnInit(): void {
    if(this.code) {
      console.log("got code", this.code)
      this.service.requestAccessToken(this.code).subscribe(success => {
        if(success)
          this.authorisationState = "success"
        else {
          this.error = "unexpected error"
          this.authorisationState = "error";
        }
      });
    } else if(this.error)
      this.authorisationState = "error"
    else
      this.authorisationState = "initial";
  }

  authorize() {
    const scope: string = "user-modify-playback-state playlist-read-private playlist-read-collaborative user-read-playback-position user-top-read user-read-recently-played user-library-read"
    const redirect: string = `${environment.UI_URL}/${appRoutes.ROOT}/${appRoutes.SPOTIFY_CALLBACK}`;
    this.state = crypto.randomUUID();

    window.location.href = `${this.SPOTIFY_AUTH_URL}?response_type=code&client_id=${this.client.clientId}&scope=${scope}&redirect_uri=${redirect}&state=${this.state}`;
  }
}
