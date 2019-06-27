##pre-requirement
* install nodejs
* add `127.0.0.1 kafka` to `/etc/hosts`

##how to build containers
run `mvn clean install` 

##how to run docker containers
run `docker-compose up` 

##start ui test script
* run `npm install serve -g`
* run `serve test-script`
* open http://localhost:5000/ in browser