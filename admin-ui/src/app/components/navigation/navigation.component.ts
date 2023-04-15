import { Component } from '@angular/core';
import {appRoutes} from "../../configuration/app.config";

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.scss']
})
export class NavigationComponent {
  menuOpen: boolean = false;

  public readonly homeRoute: string = `/${appRoutes.ROOT}/${appRoutes.HOME}`;
  public readonly scenesRoute: string = `/${appRoutes.ROOT}/${appRoutes.SCENES}`;
  public readonly lightConfigsRoute: string = `/${appRoutes.ROOT}/${appRoutes.LIGHT_CONFIGS}`;
  public readonly spotifyRoute: string = `/${appRoutes.ROOT}/${appRoutes.SPOTIFY}`;
  public readonly deviceRoute: string = `/${appRoutes.ROOT}/${appRoutes.DEVICES}`;

  navigationClicked() {
    this.menuOpen = false;
    window.scrollTo(0, 0);
  }
}
