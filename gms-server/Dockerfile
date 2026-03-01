# 使用本地代码构建的 Dockerfile
FROM maven:3.9.6-amazoncorretto-21 AS builder

WORKDIR /opt/build

COPY pom.xml ./pom.xml
COPY gms-server/pom.xml ./gms-server/pom.xml
COPY gms-server/src ./gms-server/src

RUN mvn dependency:resolve

RUN mvn clean package -DskipTests

RUN mkdir result && mv ./gms-server/target/BeiDou.jar ./result/BeiDou.jar

# 构建前端 UI
FROM node:20 AS ui-builder

WORKDIR /opt/ui

COPY gms-ui/package*.json ./
RUN npm install

COPY gms-ui/ ./
RUN npm run build

# 最终镜像
FROM ubuntu:22.04

ENV LANG=zh_CN.UTF-8
ENV LC_ALL=zh_CN.UTF-8

RUN apt-get update && apt-get install -y openjdk-21-jre-headless locales && \
    sed -i 's/# zh_CN.UTF-8/zh_CN.UTF-8/' /etc/locale.gen && \
    locale-gen && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/server

COPY --from=builder /opt/build/result/BeiDou.jar ./
COPY --from=builder /opt/build/gms-server/src/main/resources/application.yml ./
COPY gms-server/wz ./wz
COPY gms-server/wz-zh-CN ./wz-zh-CN
COPY gms-server/scripts ./scripts
COPY gms-server/scripts-zh-CN ./scripts-zh-CN
COPY --from=ui-builder /opt/ui/dist ./static

EXPOSE 8686 8484 7575 7576 7577

CMD ["java", "-Xms512m", "-Xmx2048m", "-jar", "BeiDou.jar"]
