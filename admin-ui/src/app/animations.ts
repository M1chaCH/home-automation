import {animate, animateChild, group, query, style, transition, trigger} from "@angular/animations";

export const scaleUpAnimation =
  trigger('routeChangeTrigger', [
    transition('HomePage <=> ScenesPage, SpotifyPage <=> ScenesPage, HomePage <=> SpotifyPage', [
      style({ position: "relative" }), // set unified position for both leaving and entering
      query(":enter, :leave", [
        style({
          position: "absolute",
          top: 0,
          left: 0,
        })
      ]),
      query(":enter", [ // initial style for entering child
        style({
          opacity: 0,
          scale: 0.85,
        })
      ]),
      query(":leave", animateChild()), // run all animations in leaving child comp.
      group([
        query(":leave", animate("350ms ease-out", style({
          opacity: 0,
          scale: 1.15,
        }))),
        query(":enter", animate("350ms 225ms ease-in", style({
          opacity: 1,
          scale: 1,
        }))),
      ]),
    ])
  ]);