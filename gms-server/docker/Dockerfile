# Initial Docker support thanks to xinyifly
# Optimisation performed by wejrox

#############################
# BeiDou JAR creation stage #
#############################

FROM maven:3.9.6-amazoncorretto-21 as builder

RUN yum -y install glibc-common && \
    yum -y install glibc-langpack-zh 

# set locale rnv
ENV LANG=zh_CN.UTF-8
ENV LANGUAGE=zh_CN:zh
ENV LC_ALL=zh_CN.UTF-8

# Build in a separated location which won't have permissions issues.
WORKDIR /opt/server

# Any changes to the pom will affect the entire build, so it should be copied first.
COPY ./pom.xml ./pom.xml
COPY ./gms-server/src ./gms-server/src
COPY ./gms-server/pom.xml ./gms-server/pom.xml

RUN mvn clean package -U

FROM amazoncorretto:21.0.4-alpine3.20

WORKDIR /opt/server

RUN mkdir -p ./gms-server/target

COPY --from=builder /opt/server/gms-server/target/BeiDou.jar ./gms-server/target

#########################
# Server creation stage #
#########################

# Default exposure, although not required if using docker compose.
# This exposes the login server, and channels.
# Format for channels: WWCC, where WW is 75 plus the world number and CC is 75 plus the channel number (both zero indexed).
EXPOSE 8686 8484 7575 7576 7577

ENTRYPOINT ["java", "-jar", "./gms-server/target/BeiDou.jar", "--spring.config.location=./application.yml"]
