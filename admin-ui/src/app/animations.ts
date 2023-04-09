import {animate, animateChild, group, query, style, transition, trigger} from "@angular/animations";

export const scaleUpAnimation = trigger('routeChangeTrigger', [
  transition('* <=> *', [
    style({ position: "relative" }), // set unified position for both leaving and entering
    query(":enter, :leave", [
      style({
        position: "absolute",
        top: 0,
        left: 0,
        right: 0,
      })
    ], { optional: true }),
    query(":enter", [ // initial style for entering child
      style({
        opacity: 0,
        scale: 0.85,
      })
    ], { optional: true }),
    query(":leave", animateChild(), { optional: true }), // run all animations in leaving child comp.
    group([
      query(":leave", animate("250ms ease-out", style({
        opacity: 0,
        scale: 1.15,
      })), { optional: true }),
      query(":enter", animate("250ms 200ms ease-in", style({
        opacity: 1,
        scale: 1,
      })), { optional: true }),
    ]),
  ])
]);

export const tabSwitcherAnimation = trigger("tabSwitcherAnimation", [
  transition(":increment", group([
    query(":leave", [
      style({ transform: "translateX(0)" }),
      animate("300ms ease-in-out", style({ transform: "translateX(-100%)" }))
    ]),
    query(":enter", [
      style({ transform: "translateX(100%)" }),
      animate("300ms ease-in-out", style({ transform: "translateX(0)" }))
    ]),
  ])),

  transition(":decrement",  group([
    query(":leave", [
      style({ transform: "translateX(0)" }),
      animate("300ms ease-in-out", style({ transform: "translateX(100%)" }))
    ]),
    query(":enter", [
      style({ transform: "translateX(-100%)" }),
      animate("300ms ease-in-out", style({ transform: "translateX(0)" }))
    ]),
  ])),
]);