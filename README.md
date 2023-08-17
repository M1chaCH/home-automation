# My Room Automation
This repository contains my room automation system.  
My goal is to be able to control the lights, music and maybe alarms via NFC -> Apple shortcuts and a minimal web interface (which turned out to not be minimal at all).  

### Initial Plan
I will place a NFC tag on the door to my room. Then whenever I enter, I'll scan this tag with my iPhone. This will trigger an Apple shortcut that executes a remote ssh script. This script then calls the HTTP Endpoint to toggle the room.

### Result
[Room Automation](https://michu-tech.com/room-automation/admin/home) (_can't grant public access due to obvious reasons_)  
This is how the initial plan is implemented at the moment. (activate sound)   

https://user-images.githubusercontent.com/67689103/235185469-c7470b81-1dcb-4d6c-82ed-2b13836663fe.mov

## Features
### Toggle Room
This Feature toggles my room. It's something like a light switch. At the moment it toggles the power of my Yeelights and the music playing on spotify. This feature stores the "state" of the room at power off, so when powering the room back on, the state will be recovered.  
![image](https://github.com/M1chaCH/room-automation/assets/67689103/6e5a858e-f563-45d3-9d04-654bc42c11ed)

### Scenes
You can apply scenes to the room. A scene consists of light configurations and audio configuration. So you can choose what lights should start with what color and you can choose a spotify playlist to shuffle through at a certain volume.  
<img alt="scenes" src="https://github.com/M1chaCH/room-automation/assets/67689103/91c7e2d9-cb5f-4972-a881-3bcf99cf7a23" widht="33%" /> 

### Alarm
I have implemented an alarm system. You can configure multiple alarms in the UI. An alarm is coupled with a scene. So if the time of an alarm has come, then the scene is started. To not scare you out of bed, I implemented a gratuate increase of the audio volume. The max volume of the alarm is the scene audio volume.   
Once an alarm is running you can either stop it or continue the already running scene. These actions can either be done via the UI. There is a websocket connection open to all clients that lets them know once an alarm started, so you can react to it.  
Since I don't always want to open a website every morning when I get out of bed, I have configured an apple shortcut that lets me controll the running alarm via remote SSH script.  
![image](https://github.com/M1chaCH/room-automation/assets/67689103/3c319838-2328-4d00-a4e2-d311485f3cb1)  
<img alt="scenes" src="https://github.com/M1chaCH/room-automation/assets/67689103/bf2db890-a8ae-4e0d-b1d7-e83808005b77" widht="33%" /> 

### Mini spotify player
![image](https://github.com/M1chaCH/room-automation/assets/67689103/575361f7-c1d4-4576-8b1f-9b2b5e32441c)


### GUI
![image](https://github.com/M1chaCH/room-automation/assets/67689103/40e842dc-6862-4459-bc7d-c710159e3c58)

I have implemented a fancy Angular page that lets you configure and manage the entire application.
Currently, you can do the following on this page:  
You can ...
- Toggle the room
- Apply scenes
- CRUD scenes
- CRUD light configs
- CRUD yeelight devices
- CRUD Alarms
- Authorize Spotify
- View first 50 liked spotify playlists
- mini Spotify player (toggle play, skip and previous)
- restart fake spotify speaker (Raspotify on the raspi)

### UI
Also I have created a **mini** python CLI that lets you easily access the deployed environment, including the login process. This makes the whole apple shortcut remote ssh thing a lot easier. 

### Open Ideas
- nice home dashboard in the UI
- AI driven color selecting based on music theme (you can fetch the characteristics of a song from spotify, choose colors based on this) (bit ambiguous but might be cool)

## Audio / Speaker
I use spotify and their public Web API for all the Audio.  
I needed a speaker that is always connected with spotify and since I don't have a smart speaker, I had to improvise. The solution I came up with is an old Raspberry PI. There is an application called [Raspotify](https://github.com/dtcooper/raspotify). This tells Spotify that your PI is actually a smart speaker. And since the PI does not really have integrated speakers, I have connected it via USB to my music speakers...  
![image](https://github.com/M1chaCH/room-automation/assets/67689103/09641ff7-8dd0-446b-8e1a-11f550d62745)  

