import {Component} from '@angular/core';
import {Observable} from "rxjs";
import {SpotifyService} from "../../services/spotify.service";
import {ActivatedRoute} from "@angular/router";
import {MessageDistributorService} from "../../services/message-distributor.service";

@Component({
  selector: 'app-spotify.page',
  templateUrl: './spotify.page.component.html',
  styleUrls: ['./spotify.page.component.scss']
})
export class SpotifyPageComponent {
  spotifyAuthorisation$: Observable<boolean>;
  callback: boolean = false;

  spotifyCode: string | undefined;
  spotifyState: string | undefined;
  spotifyError: string | undefined;

  get spotifyClient() {
    return this.service.spotifyClient!;
  }

  constructor(
    private service: SpotifyService,
    private route: ActivatedRoute,
    private messageDistributor: MessageDistributorService,
  ) {
    this.spotifyAuthorisation$ = service.isAuthorized();

    const url = window.location.href;
    if(url.includes("callback")) {
      this.route.queryParams.subscribe(params => {
        // @ts-ignore
        this.spotifyCode = params.code;
        // @ts-ignore
        this.spotifyState = params.state;
        // @ts-ignore
        this.spotifyError = params.error;

        this.callback = true;
      });
    }
  }

  restartSpeaker() {
    this.service.restartSpeaker().subscribe(() =>
      this.messageDistributor.pushMessage("INFO", "restarted spotify speaker"));
  }
}
