import { Component } from '@angular/core';
import {ChildrenOutletContexts} from "@angular/router";
import {scaleUpAnimation} from "./animations";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  animations: [ scaleUpAnimation ],
})
export class AppComponent {

  constructor(
    private contexts: ChildrenOutletContexts
  ) { }

  getRouteAnimationData() {
    return this.contexts.getContext('primary')?.route?.snapshot?.data?.['animation'];
  }
}
