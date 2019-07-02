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