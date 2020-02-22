FROM openjdk:8-jdk-alpine
RUN addgroup -S dieb && adduser -S frequenz -G dieb
USER frequenz:dieb
WORKDIR /home/frequenz
COPY . ./frequenzdieb-server
USER root
RUN chown -R frequenz:dieb ./
USER frequenz:dieb
WORKDIR frequenzdieb-server
RUN ./gradlew clean bootJar --no-daemon --stacktrace
COPY ./build/libs ../app
RUN rm -rf ../frequenzdieb-server
ENTRYPOINT ["java","-cp","app:app/libs/*","ch.frequenzdieb.api.FrequenzdiebRestApiApplicationKt"]
