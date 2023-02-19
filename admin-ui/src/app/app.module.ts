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

@NgModule({
  declarations: [
    AppComponent,
    HomePageComponent,
    ScenesPageComponent,
    SpotifyPageComponent,
    NavigationComponent,
    PageMessagesComponent,
    SpotifyAuthorisationComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    HttpClientModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
