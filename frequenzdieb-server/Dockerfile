FROM openjdk:8-jdk-alpine
RUN addgroup -S dieb && adduser -S frequenz -G dieb
USER frequenz:dieb
VOLUME /tmp
COPY . /frequenzdieb-server
RUN ls -al && cd frequenzdieb-server && ls -al
RUN frequenzdieb-server/gradlew --no-daemon jibDockerBuild --image=ch.frequenzdieb/frequenzdieb-rest-api
ARG DEPENDENCY=build/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","ch.frequenzdieb.api.FrequenzdiebRestApiApplicationKt"]
