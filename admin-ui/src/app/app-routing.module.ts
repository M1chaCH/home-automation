import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {appRoutes} from "./configuration/app.config";
import {HomePageComponent} from "./pages/home.page/home.page.component";
import {ScenesPageComponent} from "./pages/scenes.page/scenes.page.component";
import {SpotifyPageComponent} from "./pages/spotify.page/spotify.page.component";
import {DevicePageComponent} from "./pages/device.page/device.page.component";
import {LightConfigsPageComponent} from "./pages/light-configs.page/light-configs.page.component";
import {MessageDetailPageComponent} from "./pages/message-detail.page/message-detail-page.component";
import {AlarmPageComponent} from "./pages/alarm.page/alarm.page.component";

const routes: Routes = [
  { path: "", redirectTo: `/${appRoutes.ROOT}/${appRoutes.HOME}`, pathMatch: "full" },
  { path: appRoutes.ROOT, redirectTo: `/${appRoutes.ROOT}/${appRoutes.HOME}`, pathMatch: "full" },
  {
    path: appRoutes.ROOT,
    children: [
      { path: appRoutes.HOME, component: HomePageComponent, data: { animation: "HomePage" } },
      { path: appRoutes.SCENES, component: ScenesPageComponent, data: { animation: "ScenesPage" } },
      { path: appRoutes.LIGHT_CONFIGS, component: LightConfigsPageComponent, data: { animation: "LightConfigs" } },
      { path: appRoutes.SPOTIFY, component: SpotifyPageComponent, data: { animation: "SpotifyPage" } },
      { path: appRoutes.SPOTIFY_CALLBACK, component: SpotifyPageComponent, data: { animation: "SpotifyPage" } },
      { path: appRoutes.DEVICES, component: DevicePageComponent, data: { animation: "DevicesPage" } },
      { path: appRoutes.MESSAGE_DETAILS, component: MessageDetailPageComponent, data: { animation: "MessageDetailsPage" } },
      { path: appRoutes.ALARM, component: AlarmPageComponent, data: { animation: "AlarmPageComponent" } },
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
