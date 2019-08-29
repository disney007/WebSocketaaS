## Introduction
The project is the backend solution for client to client and server to client communication via websocket, 
which highly focus on performance, scalability and reliability.
## Core concept

 ![Alt text](res/domain.png?raw=true "Domain")

One domain contains group of the connectors, processors, queues and databases. 
- Connectors are responsible for accepting client messages via web sockets and putting the messages onto the queues.
- Queues will deliver the messages to any of the processors.
- Processors process the messages and forward the messages to target client user by putting the message back onto the queue,
and specific connector will pick the message up and send to the remote user.
- Databases store the meta information and failed delivery messages

Messages defined as reliable will be persisted in database if failed to deliver to target user, e.g. user not connected.
User can pull missed messages when they connect next time. 

One connectors can handle 1K users connected at the same time, if one domain has 10 connectors, it roughly can support
10K users. If more users need to connect, it needs to link multiple domains together to form a domain network, the following diagram
shows a simple domain network.

![Alt text](res/multi-domains.png?raw=true "Multi-Domain")

If a user on domain 1 wants to send a message to a user in domain 4, then
* user sends the message to connector of domain 1
* Processor in domain 1 will get the message and try to figure out in which domain the receiver is and pass it next domain in shortest path.
* Processor in domain 5 will receive th message and do the same steps as the processor in 1 does, and it will deliver message to domain 4
* Processor in domain 4 receives the message finally and sends to connector and connector forwards to target user.
  
Message will be persisted on the domain during the delivery if error occurred, and it will continue when problem gets resolved.
## Live demo
- open [https://spendzer.app](https://spendzer.app) in browser
- click `connect` button to connect to `sandbox-linker.spendzer.app` with port number `443` in websocket
- click `auth` button to login with randomly generated user id
- `AUTH_CLIENT_REPLY` will show up
- open [https://spendzer.app](https://spendzer.app) in another tab
- do the same steps and auth with another user
- you can send messages between the two users in section `MESSAGE` now
- enjoy
## Performance benchmark test
- application was deployed in kubernetes cluster under google cloud platform with 3 nodes, 1 CPU for each and 4GB memory in total.
- the cluster has 2 connectors, 2 processors, 1 kafka, 1 nats, 1 redis, 1 mongodb and 1 nginx deployed. 
- tests were performed in 2 laptops (each laptop has 8 cores, 16GB)
- each laptop opens 2 processes and each process issues 1000 connections and keeps sending messages to itself every 10ms.
- each message has 1000 characters as payload.
- time is measured between before sending the message and after receiving the same message.
- the table contains the time (million seconds) per each message.

|  MessageType  | HTTPS Connection | 1000 Connections | 2000 Connections | 3000 Connections | 4000 Connections
|---------------|------------------|------------------|------------------|------------------|-----------------
| RELIABLE      |   200 ~ 220ms    |    45 ~ 70ms     |   70 ~ 100ms     |  100 ~ 170ms     | 170 ~ 220ms
| FAST          |   200 ~ 220ms    |    35 ~ 50ms     |   35 ~ 50ms      |   35 ~ 50ms      |  35 ~ 50ms

## Running on your local machine in single domain
![](res/linker.gif)
### Prerequisites
* install docker
* install jdk8
* add `127.0.0.1 kafka` to `/etc/hosts`
* checkout project and goto project root folder

### Building and Running
* `mvn clean install` builds the project and docker containers for connector, processor and test-script
* `docker-compose -f docker-required-services.yml up` starts required services
* `docker-compose up` starts application services

`mvn clean install -DskipBuildingDocker` only builds the project without building docker images.
### Testing with a sample UI
the docker container `test-script` will be up and running automatically. Open `http://localhost:4400` in browser.

if application services are running inside docker container, the default websocket port is `8088`; 
if application services are running outside of docker container, the port is `8089` and resolve hostname `kafka` 
to `127.0.0.1` is required

### Examples
suppose application servers are running in docker and connect with javascript
##### Connect to the server using a WebSocket
```javascript
var ws = new WebSocket("ws://localhost:8088/ws");
```
##### Authentication
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
##### Authentication with client application
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
#### Send message to another connected user
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

#### Receive message from the server
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
#### Send message to multiple users
```json
{
    "type": "GROUP_MESSAGE",
    "data": {
        "to": ["ANZ-1232121", "ANZ-1232122"],
        "content": "some thing here to send"
    },     
    "reference":"abc123"
}
```
* the above message will be sent to user `ANZ-1232121` and `ANZ-1232122`
* the message confirmation is not available for `GROUP_MESSAGE`
#### Fetch missing/missed messages
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
### Client application configuration
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

## Running on your local machine with multiple domains
It's good practice to run one domain in one virtual machine, if there are 4 domains, then create 4 virtual machines. 
### Meta Server
All domains
should have access to the share meta server to load the domain network graph. The meta server docker images is generated during `mvn clean install`.
it can start the meta server instance in the host machine and given the url to domains. The following is the sample configuration of meta server.
``` yaml
spring:
  profiles: development
server:
  port: 9000

domains:
  - name: domain-01
    urls:
      - tcp://192.168.56.1:9089
      - ws://192.168.56.1:8088
  - name: domain-02
    urls:
      - tcp://192.168.56.2:9089
      - ws://192.168.56.2:8088
  - name: domain-03
    urls:
      - tcp://192.168.56.3:9089
      - ws://192.168.56.3:8088
  - name: domain-04
    urls:
      - tcp://192.168.56.4:9089
      - ws://192.168.56.4:8088

domainLinks:
  - n1: domain-01
    n2: domain-02
  - n1: domain-02
    n2: domain-03
  - n1: domain-03
    n2: domain-04
  - n1: domain-01
    n2: domain-04

clientApps:
  - appName: domain1
    appId: domain1
    masterUserId: domain1-1
    authEnabled: false
  - appName: domain2
    appId: domain2
    masterUserId: domain2-1
    authEnabled: false
  - appName: domain3
    appId: domain3
    masterUserId: domain3-1
    authEnabled: false
  - appName: domain4
    appId: domain4
    masterUserId: domain4-1
    authEnabled: false
  - appName: ANZ
    appId: app-id-343
    masterUserId: ANZ-123223
    authUrl: http://localhost:4200/auth
    authEnabled: false
    userDistributions:
      - domainName: domain-01
        from: 0
        to: 999999
      - domainName: domain-02
        from: 1000000
        to: 2999999
      - domainName: domain-03
        from: 3000000
        to: 5999999
      - domainName: domain-04
        from: 6000000
``` 
- `domains`: defines the name and the urls, the `ws` is websocket for end user, `tcp` is the internal communication between domains.
The ip addresses must be changed according to your real environment.
- `domainLinks`: defines the link between the domains, `n1` and `n2` are bidirectional
- `clientApps`: defines the client information for end user clients and domain clients, if domain A is linked to domain B, then domain A is the client of domain B
- `userDistributions`: defines which user is localed on which domain, e.g. `ANZ-2000000` is on `domain-02`

### Multi domain docker compose file
the docker compose file template in provided in `docker-multip-domain-compose`
```
version: '3'
services:
  connector-02:
    image: linker/connector:latest
    environment:
      SPRING_PROFILES_ACTIVE: development-multi-domain
      domainName: domain-04
      connectorName: connector-02
      wsPort: 8088
      tcpPort: 9089
      kafkaHosts: kafka:29092
      natsHosts: nats://nats:4222
      SPRING_REDIS_HOST: redis
    ports:
      - "8088:8088"
      - "9089:9089"
  processor-02:
    image: linker/processor:latest
    environment:
      SPRING_PROFILES_ACTIVE: development-multi-domain
      domainName: domain-04
      processorName: processor-02
      kafkaHosts: kafka:29092
      natsHosts: nats://nats:4222
      metaServerUrl: http://192.168.56.1:9000
      SPRING_REDIS_HOST: redis
      SPRING_DATA_MONGODB_HOST: mongodb
```
the important point in here is the `metaServerUrl`, which must be the meta server url hosted in your local, and change 
the `domainName` to the real domain name accordingly

### Start docker instances
Logon to virtual machine and copy the docker compost files there.
* `docker-compose -f docker-required-services.yml up` starts required services
* `docker-compose up` starts application services
### How to test on ui
`IMPROTANT` the users on the user distribution should login to the CORRECT domain, e.g. `ANZ-123456` should go to `domain-01`,
`ANZ-3123456` should go to `domain-03`
Suppose we want to login with `ANZ-3123456`, then open the url in browser `http://192.168.56.3:4400`, and connect to websocket `ws://192.168.56.3:8088`, 
then do auth and send message to another user
