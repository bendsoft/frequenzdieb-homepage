FROM openjdk:8-jdk-alpine
RUN addgroup -S dieb && adduser -S frequenz -G dieb
USER frequenz:dieb
VOLUME /tmp
COPY . /tmp/frequenzdieb-server
RUN /tmp/frequenzdieb-server/gradlew --no-daemon --no-build-cache jibDockerBuild --image=ch.frequenzdieb/frequenzdieb-rest-api
ARG DEPENDENCY=/tmp/frequenzdieb-server/build/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
RUN rm -rf /tmp/frequenzdieb-server
ENTRYPOINT ["java","-cp","app:app/lib/*","ch.frequenzdieb.api.FrequenzdiebRestApiApplicationKt"]
