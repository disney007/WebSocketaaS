FROM openjdk:8-jre-alpine
ARG JAR_FILE
ENV SPRING_PROFILES_ACTIVE ''
ENV domainName ''
ENV connectorName ''
ENV wsPort ''
ENV kafkaHosts ''
ENV natsHosts ''
RUN mkdir -p "/opt/data/"
COPY "$JAR_FILE" "/opt/data/connector.jar"
WORKDIR "/opt/data"
ENTRYPOINT ["java", "-jar", "connector.jar"]
