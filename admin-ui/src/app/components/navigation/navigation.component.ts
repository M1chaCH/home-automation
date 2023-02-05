import { Component } from '@angular/core';
import {appRoutes} from "../../configuration/app.config";

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.scss']
})
export class NavigationComponent {

  public readonly homeRoute: string = `/${appRoutes.ROOT}/${appRoutes.HOME}`;
  public readonly scenesRoute: string = `/${appRoutes.ROOT}/${appRoutes.SCENES}`;
  public readonly spotifyRoute: string = `/${appRoutes.ROOT}/${appRoutes.SPOTIFY}`;
}
