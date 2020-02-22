FROM openjdk:8-jdk-alpine
RUN addgroup -S dieb && adduser -S frequenz -G dieb
USER frequenz:dieb
WORKDIR /home/frequenz
COPY . ./frequenzdieb-server
RUN ls -al && ls -al /frequenzdieb-server
USER root
RUN chown -R frequenz:dieb ./
USER frequenz:dieb
RUN frequenzdieb-server/gradlew bootJar --debug --stacktrace
COPY frequenzdieb-server/build/libs ../app
RUN rm -rf ../frequenzdieb-server
ENTRYPOINT ["java","-cp","app:app/libs/*","ch.frequenzdieb.api.FrequenzdiebRestApiApplicationKt"]
