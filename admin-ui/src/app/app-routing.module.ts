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
      { path: appRoutes.HOME, component: HomePageComponent, data: { animation: "HomePage" } },
      { path: appRoutes.SCENES, component: ScenesPageComponent, data: { animation: "ScenesPage" } },
      { path: appRoutes.SPOTIFY, component: SpotifyPageComponent, data: { animation: "SpotifyPage" } },
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
