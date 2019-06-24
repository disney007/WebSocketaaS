FROM openjdk:8-jdk-alpine
ENV SPRING_PROFILES_ACTIVE ''
ENV domainName ''
ENV connectorName ''
RUN mkdir -p "/opt/data/"
COPY "connector/target/connector-1.0-SNAPSHOT.jar" "/opt/data/connector.jar"
WORKDIR "/opt/data"
ENTRYPOINT ["java", "-jar", "connector.jar"]