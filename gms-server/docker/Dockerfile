# Initial Docker support thanks to xinyifly
# Optimisation performed by wejrox

#############################
# BeiDou JAR creation stage #
#############################

FROM maven:3.9.6-amazoncorretto-21

RUN yum -y install glibc-common && \
    yum -y install glibc-langpack-zh 

# set locale rnv
ENV LANG=zh_CN.UTF-8
ENV LANGUAGE=zh_CN:zh
ENV LC_ALL=zh_CN.UTF-8

# Build in a separated location which won't have permissions issues.
WORKDIR /opt/server

# Any changes to the pom will affect the entire build, so it should be copied first.
COPY ../pom.xml ./pom.xml

# Grab all the dependencies listed in the pom early, since it prevents changes to source code from requiring a complete re-download.
# Skip compiling tests since we don't want all the dependencies to be downloaded.
# RUN mvn -f ./pom.xml clean dependency:go-offline -Dmaven.test.skip -T 1C
# TODO: The above command stopped working as of Java 21 upgrade due to:
# Failed to execute goal org.apache.maven.plugins:maven-dependency-plugin:3.6.1:go-offline (default-cli) on project Cosmic: org.eclipse.aether.resolution.DependencyResolutionException: The following artifacts could
# not be resolved: io.netty:netty-tcnative:jar:${os.detected.classifier}:2.0.65.Final (absent): Could not find artifact io.netty:netty-tcnative:jar:${os.detected.classifier}:2.0.65.Final in central (https://repo.maven.apache.org/maven2) -> [Help 1]

# Source code changes may not change dependencies, so it can go last.
# Skip compiling tests since we don't want all the dependecies to be downloaded for plugins.
COPY ../src ./src

RUN mvn clean package

#########################
# Server creation stage #
#########################

# Scripts are sourced on server startup, so you can mount over them for quicker redeploy.
# COPY ./docker-application.yml ./docker-application.yml

# Default exposure, although not required if using docker compose.
# This exposes the login server, and channels.
# Format for channels: WWCC, where WW is 75 plus the world number and CC is 75 plus the channel number (both zero indexed).
EXPOSE 8484 7575 7576 7577

ENTRYPOINT ["java", "-jar", "./target/BeiDou.jar"]
