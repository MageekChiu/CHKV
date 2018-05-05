# 在外部构建好jar
FROM openjdk:8-jdk-alpine
ENV PROJECT_PATH=/var/netserver
ENV PATH=$PATH:$PROJECT_PATH/
ENV TZ=Asia/Shanghai
RUN mkdir -p $PROJECT_PATH
COPY . $PROJECT_PATH
WORKDIR $PROJECT_PATH
RUN echo $TZ | tee /etc/timezone

## 构建过程放在容器内部就必须装maven
#FROM registry.cn-hangzhou.aliyuncs.com/acs/maven:3-jdk-8
#ENV PROJECT_PATH=/var/netserver
#ENV PATH=$PATH:$PROJECT_PATH/
#ENV TZ=Asia/Shanghai
#RUN mkdir -p $PROJECT_PATH
#COPY . $PROJECT_PATH
#WORKDIR $PROJECT_PATH
#RUN echo $TZ | tee /etc/timezone && mvn install dockerfile:build