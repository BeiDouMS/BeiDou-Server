# Initial Docker support thanks to xinyifly

#
# Build stage
#
FROM maven:3.6.3-jdk-8 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#
FROM openjdk:8
COPY --from=build /home/app/target/Cosmic.jar /usr/local/lib/Cosmic.jar
COPY ./ ./
EXPOSE 8484 7575 7576 7577
ENTRYPOINT ["java", "-jar", "/usr/local/lib/Cosmic.jar"]


