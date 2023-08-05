import http.client
import json
import ssl
import sys

AUTH_COOKIE_FILE = "login.txt"
triedAuthenticating = False

def printHelp():
    print("Use this script to access the production room-automation REST API")
    print("arg1: username")
    print("arg2: password")
    print("arg3: type - either: 'scene', 'room', 'alarm', 'help'")
    print("arg4: parameter to the request (for scene: scene id (number), for alarm: 'stop' / 'continue')")

def executeAuthenticatedRequest(username, password, path, method):
    global triedAuthenticating
    print("executing " + method + ":michu-tech.com" + path)

    conn = http.client.HTTPSConnection(
        "michu-tech.com",
        context = ssl._create_unverified_context()
    )

    headers = {
        'Cookie': loadRequestCookie(username, password, False)
    }

    conn.request(method, path, "", headers)
    res = conn.getresponse()
    if(triedAuthenticating != True and (res.status == 401 or res.status == 403)): 
        loadRequestCookie(username, password, True)
        triedAuthenticating = True
        return executeAuthenticatedRequest(username, password, path, method)
    else: triedAuthenticating = False

    print("successfully executed request")
    return res

def handleSceneRequest(username, password, parameter = 0):
    print("starting scene with id", parameter)
    res = executeAuthenticatedRequest(username, password, "/room-automation/api/automation/scene/" + parameter, "POST")
    print("status:", res.status, "answer:", res.read().decode("utf-8"))

def handleRoomRequest(username, password): 
    print("toggling room")
    res = executeAuthenticatedRequest(username, password, "/room-automation/api/automation", "PUT")
    print("status:", res.status, "answer:", res.read().decode("utf-8"))

def handleAlarmRequest(username, password, parameter): 
    if(parameter == "stop"): 
        print("stopping alarm")
        res = executeAuthenticatedRequest(username, password, "/room-automation/api/automation/alarm/current", "DELETE")
        print("status:", res.status, "answer:", res.read().decode("utf-8"))
    elif(parameter == "continue"):
        print("continuing alarm")
        res = executeAuthenticatedRequest(username, password, "/room-automation/api/automation/alarm/current", "PUT")
        print("status:", res.status, "answer:", res.read().decode("utf-8"))
    else: 
        print("missing or invalid parameter:", parameter)

def loadRequestCookie(username = "", password = "", forceLogin = False): 
    if(not forceLogin):
        try:
            print("checking auth file at", AUTH_COOKIE_FILE)
            with open(AUTH_COOKIE_FILE, "r") as file:
                print("found auth file, returning content")
                return file.read()
        except FileNotFoundError:
            print("file not found")

    print("performing login")        

    conn = http.client.HTTPSConnection(
        "michu-tech.com",
        context = ssl._create_unverified_context()
    )
    payload = json.dumps({
        "mail": "micha_ch@outlook.com",
        "password": "M1chAHome*11"
    })
    headers = {
        'Content-Type': 'application/json'
    }
    conn.request("POST", "/root/security/login", payload, headers)
    res = conn.getresponse()
    authCookie = res.headers.get("Set-Cookie")
    with open(AUTH_COOKIE_FILE, "w") as file:
        file.write(authCookie)
    print("successfully fetched and stored new authCookie")    
    return authCookie

# def main(sys.argv): (don't like python, need an entry point, even if it is fake (;) 
username = ""
password = ""
requestType = ""
requestParam = ""
if(len(sys.argv) > 1): username = sys.argv[1]
if(len(sys.argv) > 2): password = sys.argv[2]
if(len(sys.argv) > 3): requestType = sys.argv[3]
if(len(sys.argv) > 4): requestParam = sys.argv[4]

print("got args:", username,  "" if password == "" else "******", requestType, requestParam)

if(username == "" or username == "help" or requestType == "help"): printHelp()
elif(requestType == "scene"): handleSceneRequest(username, password, requestParam)
elif(requestType == "room"): handleRoomRequest(username, password)
elif(requestType == "alarm"): handleAlarmRequest(username, password, requestParam)

print("scrip completed")
