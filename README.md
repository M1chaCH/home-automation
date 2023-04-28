# My Room Automation
This repository contains my room automation system.  
My goal is to be able to control the lights, music and maybe alarms via NFC -> Apple shortcuts and a minimal web interface.  

### Initial Plan
I will place a NFC tag on the door to my room. Then whenever I enter, I'll scan this tag with my iPhone. This will trigger an Apple shortcut that executes a remote ssh script. This script then calls the HTTP Endpoint to toggle the room.

### Result
This is how the initial plan is implemented at the moment. (activate sound)   

https://user-images.githubusercontent.com/67689103/235185469-c7470b81-1dcb-4d6c-82ed-2b13836663fe.mov

## Run with docker
1. clone this repo
2. fill API properties (docker-compose env vars)
3. change URLs in the UI config (room-automation/admin-ui/src/environments/environment.ts)
4. build: `docker/docker-compose build`
5. run: `docker/docker-compose up`

## Features
### Toggle Room
This Feature toggles my room. It's something like a light switch. At the moment it toggles the power of my Yeelights and the music playing on spotify. 

### UI
I have implemented a fancy Angular page that lets you configure and manage the entire application.
Currently, you can do the following on this page:  
You can ...
- Toggle the room
- Apply scenes
- CRUD scenes
- CRUD light configs
- CRUD yeelight devices
- Authorize Spotify
- View first 50 liked spotify playlists
- mini Spotify player (toggle play, skip and previous)
- restart fake spotify speaker (Raspotify on the raspi)

### Planned features
- alarm system to wake me up
- map a spotify playlist to a scene (in the UI, backend already exists)

## Audio
I use spotify and their public Web API for all the Audio.  
I needed a speaker that is always connected with spotify and since I don't have a smart speaker, I had to improvise. The solution I came up with is an old Raspberry PI. There is an application called [Raspotify](https://github.com/dtcooper/raspotify). This tells Spotify that your PI is actually a smart speaker. And since the PI does not really have integrated speakers, I have connected it via USB to my music speakers.  
