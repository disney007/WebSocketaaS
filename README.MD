##how to build containers
- build all the projects `mvn clean install`
- build connector `mvn package -pl connector`
- build processor `mvn package -pl processor`

##how to run docker containers
- start connector instance `docker run -e "SPRING_PROFILES_ACTIVE=sandbox" -e "domainName=01" -e "connectorName=01" linker.spendzer.com/connector:version`
- start processor instance `docker run -e "SPRING_PROFILES_ACTIVE=sandbox" -e "domainName=01" -e "processorName=01" linker.spendzer.com/processor:vesrion`
