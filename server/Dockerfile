FROM openjdk:17-jre-slim
RUN addgroup --system dieb
RUN adduser --system --ingroup dieb frequenz
USER frequenz:dieb
VOLUME /tmp
ARG DEPENDENCY=build/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","ch.frequenzdieb.api.FrequenzdiebRestApiApplicationKt"]
