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
server does not actually valid the user and replies successfully authenticated
```javascript 
{ 
    "type": "AUTH_CLIENT_REPLY", 
    "data": { 
        "appId": "app-id-343", 
        "userId": "ANZ-123223", 
        "token": "token-12345", 
        "result": { 
            "status": "OK" 
           } 
        }, 
    "feature": "RELIABLE" 
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
             "feature":"RELIABLE"
          };
    ws.send(JSON.stringify(message));
```
* `data.to` is the target user to send, `data.content` is the actual information to send
* `feature` can be `RELIABLE` and `FAST`, the default value is `RELIABLE`.  
`RELIABLE` guarantees message will not get lost and safely routed to target user, but relatively slow. 
`FAST` messages are only in memory, which might get lost if computer suddenly shutdown, but fast.
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
        *           "content":"some thing here to send"},
        *           "feature":"RELIABLE"
        *       }
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
    },
    "feature": "FAST"
}
// ...
// other missing messages
// ...
{
    "type": "FETCH_MISSING_MESSAGES_COMPLETE",
    "data": {
        "count": 25
    }
}
```