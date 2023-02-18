import {Component, OnInit} from '@angular/core';
import {ChildrenOutletContexts} from "@angular/router";
import {scaleUpAnimation} from "./animations";
import {SpotifyService} from "./services/spotify.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  animations: [ scaleUpAnimation ],
})
export class AppComponent implements OnInit{

  constructor(
    private contexts: ChildrenOutletContexts,
    private spotifyService: SpotifyService,
  ) { }

  ngOnInit(): void {
    this.spotifyService.loadClient();
  }

  getRouteAnimationData() {
    return this.contexts.getContext('primary')?.route?.snapshot?.data?.['animation'];
  }
}
