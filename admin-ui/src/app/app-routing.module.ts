import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {appRoutes} from "./configuration/app.config";
import {HomePageComponent} from "./pages/home.page/home.page.component";
import {ScenesPageComponent} from "./pages/scenes.page/scenes.page.component";
import {SpotifyPageComponent} from "./pages/spotify.page/spotify.page.component";

const routes: Routes = [
  { path: "", redirectTo: `/${appRoutes.ROOT}/${appRoutes.HOME}`, pathMatch: "full" },
  { path: appRoutes.ROOT, redirectTo: `/${appRoutes.ROOT}/${appRoutes.HOME}`, pathMatch: "full" },
  {
    path: appRoutes.ROOT,
    children: [
      { path: appRoutes.HOME, component: HomePageComponent },
      { path: appRoutes.SCENES, component: ScenesPageComponent },
      { path: appRoutes.SPOTIFY, component: SpotifyPageComponent },
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
