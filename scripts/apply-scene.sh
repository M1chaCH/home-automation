#!/bin/bash

SCENE_ID=$1

echo applying scene $SCENE_ID

curl --location --request PUT 'michus-ras-env:8080/automation' --header 'Content-Type: application/json' --data '{"sceneId":"'$SCENE_ID'"}'
 
echo script completed
