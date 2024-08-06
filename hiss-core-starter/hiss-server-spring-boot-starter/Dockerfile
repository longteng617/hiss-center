FROM openjdk:11-jdk

# 时区修改为东八区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 装在程序
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app.jar

# 装在模板
RUN cd /root && mkdir template
RUN chmod -R 775 /root/template
COPY template /root/template

EXPOSE 8081
EXPOSE 10003
ENTRYPOINT ["sh","-c","java -Dspring.profiles.active=prod -jar $JAVA_OPTS /app.jar"]
