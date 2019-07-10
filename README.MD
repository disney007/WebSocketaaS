## introduction
The project is the backend solution for client to client and server to client communication via websocket, 
which highly focus on performance, scalability and reliability.
 
## how to run application in local
### pre-requirement
* install docker
* install jdk8
* add `127.0.0.1 kafka` to `/etc/hosts`
* checkout project and goto project root folder

### how to start application
* `mvn clean install` builds the project and docker containers
* `docker-compose -f docker-required-services.yml up` starts required services
* `docker-compose up` starts application services

### how to test on ui
serve folder `test-script` as webservice, here `npm serve` is used
* install serve `npm install serve -g`
* run `serve test-script`
* open http://localhost:5000/ in browser

if application services are running inside docker container, the default websocket port is `8088`; 
if application services are running outside of docker container, the port is `8089` and resolve hostname `kafka` 
to `127.0.0.1` is required

### example
suppose application servers are running in docker and connect with javascript
##### connect to server with websocket
```javascript
var ws = new WebSocket("ws://localhost:8088/ws");
```
##### authentication
```javascript
ws.onopen = function () {
    var message = {
          "type": "AUTH_CLIENT",
          "data": {
              "appId": "app-id-343",
              "userId": "ANZ-123223",
              "token": "token-12345"
          }
      };
    ws.send(JSON.stringify(message));
};
```
- `appId` is the arbitrary string, e.g uuid
- `userId` the current user who connects to server, and the only identifier to send and receive messages, the structure of user id must be 
`app short name` + `-` + `numbers`, e.g `ANZ-123223`
- `token` is the reversed keyword which will be used in authentication together with client application in the future

server simply verifies the appId and user prefix and replies successfully authenticated
```javascript 
{ 
    "type": "AUTH_CLIENT_REPLY", 
    "data": { 
        "appId": "app-id-343", 
        "userId": "ANZ-123223",        
        "isAuthenticated": true
     }
}
```
the appId `app-id-343` and userId prefix `ANZ` must match the client app settings defined in application.properties
##### authentication with client application
the authentication with client is disabled by default, which can be enabled in application.properties
```
clientApps[0].authUrl=http://localhost:8081/auth
clientApps[0].authEnabled=false
``` 
if `authEnabled` set to `true`, then when a user login it will send a http post request to `authUrl` with request body as follow
```json
{
  "appId": "app-id-343", 
  "userId": "ANZ-123223", 
  "token": "token-12345" 
}
```
And client endpoint must reply with following json structure
```json
{
  "appId": "app-id-343", 
  "userId": "ANZ-123223", 
  "isAuthenticated": true
}
```
#### send message to another connected user
```javascript
    var message = {
          "type": "MESSAGE",
          "data": {
              "to": "ANZ-1232121",
              "content": "some thing here to send"
          },
         "feature":"RELIABLE",
         "reference":"abc123"
      };
    ws.send(JSON.stringify(message));
```
* `data.to` is the target user to send, `data.content` is the actual information to send
* `feature` can be `RELIABLE` and `FAST`, the default value is `RELIABLE`.  
`RELIABLE` guarantees message will not get lost and safely routed to target user, but relatively slow. 
`FAST` messages are only in memory, which might get lost if computer suddenly shutdown, but fast.
* `reference` is optional, if it's given and `confirmationEnabled` is set to `true` (default is `true`), `MESSAGE_CONFIRMATION` message will be received when current message successfully delivered to target user.
Default value is `null`, which means no confirmation message will send back.

```javascript 
{
    "type": "MESSAGE_CONFIRMATION", 
    "data": {
        "reference": "abc123"
    }
}
```
The confirmation message is always sent with feature `RELIABLE`, which guarantees the confirmation message will not get
lost even if the sender is not connected at the moment. 

#### receive message from server
if message received from server, `onmessage` will be fired.
```javascript
    ws.onmessage = function (event){
        var message = JSON.parse(event.data);
        /*
        * if the message was sent from another user, the message will be
        *  { 
        *       "type":"MESSAGE",
        *       "data": {
        *           "from":"ANZ-123223",
        *           "content":"some thing here to send"       
        *       },
        *       "reference":"abc123"
        * }       
        * */
        console.log(message);           
    };
```
#### fetch missing messages
if target user is not found (connected and authenticated), the message will be persisted in database. The target user can 
fetch the missing messages when login next time.\
\
user sends with following request
```javascript 

{
    "type": "FETCH_MISSING_MESSAGES_REQUEST",
    "data": {
        "count": 100
    }
}

```
user will receive missing messages and FETCH_MISSING_MESSAGES_COMPLETE message at the end, which indicates currently fetching procedure is done and how many left,
it can trigger another fetch
``` javascript
{
    "type": "MESSAGE",
    "data": {
        "from": "ANZ-123223",
        "content": "some thing here to send"
    }
}
// ...
// other missing messages
// ...
{
    "type": "FETCH_MISSING_MESSAGES_COMPLETE",
    "data": {
        "leftMissingCount": 25
    }
}
```
#### client application configurations
client application settings are in application properties, it also can config multiple client applications
```javascript
clientApps[0].appName=ANZ
clientApps[0].appId=app-id-343
clientApps[0].masterUserId=ANZ-123223
```
- `appName` is the application name or short name and also the prefix for user id, which should be unique between client applications
- `appId` is a long unique string, uuid would be the best
- `masterUserId` is the user id that can send and receive messages like other users, the only difference is that
the master user id can receive `USER_DISCONNECTED` and `USER_CONNECTED` messages of other users

