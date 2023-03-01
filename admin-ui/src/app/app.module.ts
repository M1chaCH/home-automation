import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomePageComponent } from './pages/home.page/home.page.component';
import { ScenesPageComponent } from './pages/scenes.page/scenes.page.component';
import { SpotifyPageComponent } from './pages/spotify.page/spotify.page.component';
import { NavigationComponent } from './components/navigation/navigation.component';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {HttpClientModule} from "@angular/common/http";
import {PageMessagesComponent} from "./components/page-messages/page-messages.component";
import { SpotifyAuthorisationComponent } from './components/spotify/spotify.authorisation/spotify-authorisation.component';
import { SpotifyResourcesComponent } from './components/spotify/spotify.resources/spotify.resources.component';
import { FancyButtonComponent } from './components/fancy-button/fancy-button.component';
import { DevicePageComponent } from './pages/device.page/device.page.component';
import {FormsModule} from "@angular/forms";

@NgModule({
  declarations: [
    AppComponent,
    HomePageComponent,
    ScenesPageComponent,
    SpotifyPageComponent,
    NavigationComponent,
    PageMessagesComponent,
    SpotifyAuthorisationComponent,
    SpotifyResourcesComponent,
    FancyButtonComponent,
    DevicePageComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
