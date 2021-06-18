FROM adoptopenjdk/openjdk11:latest
VOLUME /tmp

# Create app directory
WORKDIR /opt/app

# Adding app jar
COPY parking.jar /opt/app/parking.jar

# Ports
EXPOSE 8080

# Run server
RUN ls -la
ENTRYPOINT ["java", "-jar", "/opt/app/parking.jar"]