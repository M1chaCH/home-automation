# My Room Automation
This repository contains my room automation system.  
My goal is to be able to control the lights, music and maybe alarms via NFC -> Apple shortcuts and a minimal web interface.  

Initially I will place an NFC tag on the door to my room. Then whenever I enter, I would scan the NFC tag with my iPhone. This will trigger a small app that sets up the light, music etc. 

## Apply scene on remote pc
This is a description of my current deployed setup. If you set your environment up like this it will work. But obviously you can implement it in what ever way you want to. 

- install java 17 on remote pc
- place the jar and lib files of the backend-service in `/var/apps/room-automation/`.
- place the `apply-scene.sh` in the same directory
- configure a Apple shortcut automation which executes a script on a remote server via ssh. 
    - the script of the shortcut can be like `apply-shortcut.txt`
- run the backend-service on the remote pc
    - `java -jar room-automation.jar`
